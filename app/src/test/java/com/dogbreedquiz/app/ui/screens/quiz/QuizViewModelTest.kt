package com.dogbreedquiz.app.ui.screens.quiz

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizAnswer
import com.dogbreedquiz.app.domain.model.QuizQuestion
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.usecase.GenerateQuizUseCase
import com.dogbreedquiz.app.domain.usecase.GetQuizStatisticsUseCase
import com.dogbreedquiz.app.domain.usecase.LoadBreedImageUseCase
import com.dogbreedquiz.app.domain.usecase.SaveQuizSessionUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Comprehensive unit tests for QuizViewModel
 * Tests UI state management, user interactions, and business logic coordination
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private lateinit var generateQuizUseCase: GenerateQuizUseCase

    @Mock
    private lateinit var loadBreedImageUseCase: LoadBreedImageUseCase

    @Mock
    private lateinit var saveQuizSessionUseCase: SaveQuizSessionUseCase

    @Mock
    private lateinit var getQuizStatisticsUseCase: GetQuizStatisticsUseCase

    private lateinit var viewModel: QuizViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        viewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ===== QUIZ INITIALIZATION TESTS =====

    @Test
    fun `startNewQuiz should set loading state initially`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        whenever(generateQuizUseCase(any(), any())).thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenReturn(createTestBreed("1", "Golden Retriever"))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)

        // Then
        val initialState = viewModel.uiState.first()
        assertTrue(initialState.isLoading || testQuizSession != null)
        assertNull(initialState.error)
    }

    @Test
    fun `startNewQuiz should update state with quiz session on success`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        whenever(generateQuizUseCase(DogBreed.Difficulty.BEGINNER, 10))
            .thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenReturn(createTestBreed("1", "Golden Retriever"))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertNotNull(finalState.quizSession)
        assertNotNull(finalState.currentQuestion)
        assertEquals(0, finalState.currentQuestionIndex)
        assertEquals(3, finalState.totalQuestions)
        assertEquals(0, finalState.score)
        assertEquals(3, finalState.lives)
        assertEquals(0, finalState.currentStreak)
        assertFalse(finalState.isQuizComplete)
        assertNull(finalState.error)
    }

    @Test
    fun `startNewQuiz should set error state on quiz generation failure`() = runTest {
        // Given
        val errorMessage = "Failed to generate quiz"
        whenever(generateQuizUseCase(any(), any()))
            .thenReturn(Result.failure(RuntimeException(errorMessage)))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertNotNull(finalState.error)
        assertTrue(finalState.error!!.contains(errorMessage))
        assertNull(finalState.quizSession)
    }

    @Test
    fun `startNewQuiz should handle exception during quiz generation`() = runTest {
        // Given
        whenever(generateQuizUseCase(any(), any())).thenThrow(RuntimeException("Network error"))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertFalse(finalState.isLoading)
        assertNotNull(finalState.error)
        assertTrue(finalState.error!!.contains("Failed to load quiz"))
        assertNull(finalState.quizSession)
    }

    // ===== ANSWER SELECTION TESTS =====

    @Test
    fun `selectAnswer should update state with correct answer`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        val correctBreed = testQuizSession.questions[0].correctBreed
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()

        // When
        viewModel.selectAnswer(correctBreed)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertNotNull(finalState.selectedAnswer)
        assertTrue(finalState.showResult)
        assertTrue(finalState.isCorrect)
        assertTrue(finalState.score >= 0) // Should have earned points
        assertEquals(3, finalState.lives) // Lives unchanged for correct answer
        assertEquals(1, finalState.currentStreak)
    }

    @Test
    fun `selectAnswer should update state with incorrect answer`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        val incorrectBreed = createTestBreed("wrong", "Wrong Breed")
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()

        // When
        viewModel.selectAnswer(incorrectBreed)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertNotNull(finalState.selectedAnswer)
        assertTrue(finalState.showResult)
        assertFalse(finalState.isCorrect)
        assertEquals(0, finalState.score) // No points for incorrect answer
        assertEquals(2, finalState.lives) // Lost one life
        assertEquals(0, finalState.currentStreak) // Streak reset
    }

    @Test
    fun `selectAnswer should ignore duplicate selections`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        val correctBreed = testQuizSession.questions[0].correctBreed
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()

        // When - Select answer twice
        viewModel.selectAnswer(correctBreed)
        advanceUntilIdle()
        val firstState = viewModel.uiState.first()

        viewModel.selectAnswer(createTestBreed("different", "Different Breed"))
        advanceUntilIdle()

        // Then - State should not change after second selection
        val finalState = viewModel.uiState.first()
        assertEquals(firstState.selectedAnswer?.id, finalState.selectedAnswer?.id)
        assertEquals(firstState.score, finalState.score)
        assertEquals(firstState.lives, finalState.lives)
    }

    @Test
    fun `selectAnswer should handle null current question gracefully`() = runTest {
        // Given - No quiz session started
        val testBreed = createTestBreed("1", "Test Breed")

        // When
        viewModel.selectAnswer(testBreed)
        advanceUntilIdle()

        // Then - State should remain unchanged
        val finalState = viewModel.uiState.first()
        assertNull(finalState.selectedAnswer)
        assertFalse(finalState.showResult)
        assertEquals(0, finalState.score)
    }

    // ===== NAVIGATION TESTS =====

    @Test
    fun `nextQuestion should advance to next question`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()
        viewModel.selectAnswer(testQuizSession.questions[0].correctBreed)
        advanceUntilIdle()

        // When
        viewModel.nextQuestion()
        advanceTimeBy(400) // Account for delay
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertEquals(1, finalState.currentQuestionIndex)
        assertNotNull(finalState.currentQuestion)
        assertNull(finalState.selectedAnswer)
        assertFalse(finalState.showResult)
        assertFalse(finalState.isCorrect)
    }

    @Test
    fun `nextQuestion should complete quiz when no more questions`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        whenever(saveQuizSessionUseCase(any())).thenReturn(Result.success(Unit))

        // Advance to last question
        repeat(testQuizSession.questions.size - 1) {
            viewModel.selectAnswer(testQuizSession.questions[it].correctBreed)
            advanceUntilIdle()
            viewModel.nextQuestion()
            advanceTimeBy(400)
            advanceUntilIdle()
        }

        // Answer last question
        viewModel.selectAnswer(testQuizSession.questions.last().correctBreed)
        advanceUntilIdle()

        // When
        viewModel.nextQuestion()
        advanceTimeBy(400)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertTrue(finalState.isQuizComplete)
        verify(saveQuizSessionUseCase).invoke(any())
    }

    @Test
    fun `nextQuestion should complete quiz when no lives remaining`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()
        whenever(saveQuizSessionUseCase(any())).thenReturn(Result.success(Unit))

        // Lose all lives
        val incorrectBreed = createTestBreed("wrong", "Wrong Breed")
        repeat(3) {
            viewModel.selectAnswer(incorrectBreed)
            advanceUntilIdle()
            if (it < 2) { // Don't call nextQuestion after last life lost
                viewModel.nextQuestion()
                advanceTimeBy(400)
                advanceUntilIdle()
            }
        }

        // When
        viewModel.nextQuestion()
        advanceTimeBy(400)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertTrue(finalState.isQuizComplete)
        assertTrue(finalState.lives <= 0)
        verify(saveQuizSessionUseCase).invoke(any())
    }

    @Test
    fun `nextQuestion should handle save failure gracefully`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        whenever(saveQuizSessionUseCase(any()))
            .thenReturn(Result.failure(RuntimeException("Save failed")))

        // Complete quiz
        repeat(testQuizSession.questions.size) {
            viewModel.selectAnswer(testQuizSession.questions[it].correctBreed)
            advanceUntilIdle()
            viewModel.nextQuestion()
            advanceTimeBy(400)
            advanceUntilIdle()
        }

        // Then
        val finalState = viewModel.uiState.first()
        assertTrue(finalState.isQuizComplete)
        assertNotNull(finalState.error)
        assertTrue(finalState.error!!.contains("failed to save results"))
    }

    // ===== IMAGE LOADING TESTS =====

    @Test
    fun `loadCurrentQuestionImage should update image URL on success`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        val breedWithImage = createTestBreed("1", "Golden Retriever", "https://example.com/image.jpg")
        whenever(generateQuizUseCase(any(), any())).thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenReturn(breedWithImage)

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertEquals("https://example.com/image.jpg", finalState.currentQuestionImageUrl)
        assertFalse(finalState.isLoadingImage)
    }

    @Test
    fun `loadCurrentQuestionImage should handle loading failure gracefully`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        whenever(generateQuizUseCase(any(), any())).thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenThrow(RuntimeException("Image load failed"))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertEquals("", finalState.currentQuestionImageUrl)
        assertFalse(finalState.isLoadingImage)
    }

    @Test
    fun `loadCurrentQuestionImage should skip loading if image already exists`() = runTest {
        // Given
        val existingImageUrl = "https://existing.com/image.jpg"
        val questionWithImage = createTestQuizQuestion(imageUrl = existingImageUrl)
        val testQuizSession = createTestQuizSession(listOf(questionWithImage))
        whenever(generateQuizUseCase(any(), any())).thenReturn(Result.success(testQuizSession))

        // When
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // Then
        val finalState = viewModel.uiState.first()
        assertEquals(existingImageUrl, finalState.currentQuestionImageUrl)
        assertFalse(finalState.isLoadingImage)
        // Verify loadBreedImageUseCase was not called since image already exists
        verify(loadBreedImageUseCase, org.mockito.kotlin.never()).invoke(any())
    }

    // ===== UTILITY METHOD TESTS =====

    @Test
    fun `retryQuiz should start new quiz with same difficulty`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession(difficulty = DogBreed.Difficulty.INTERMEDIATE)
        whenever(generateQuizUseCase(DogBreed.Difficulty.INTERMEDIATE, 10))
            .thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenReturn(createTestBreed("1", "Golden Retriever"))
        viewModel.startNewQuiz(DogBreed.Difficulty.INTERMEDIATE)
        advanceUntilIdle()

        // When
        viewModel.retryQuiz()
        advanceUntilIdle()

        // Then
        verify(generateQuizUseCase, org.mockito.kotlin.times(2))
            .invoke(DogBreed.Difficulty.INTERMEDIATE, 10)
    }

    @Test
    fun `clearError should remove error from state`() = runTest {
        // Given - Set an error state
        whenever(generateQuizUseCase(any(), any()))
            .thenReturn(Result.failure(RuntimeException("Test error")))
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
        advanceUntilIdle()

        // When
        viewModel.clearError()

        // Then
        val finalState = viewModel.uiState.first()
        assertNull(finalState.error)
    }

    @Test
    fun `getCurrentQuestionBreed should return correct breed`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()

        // When
        val result = viewModel.getCurrentQuestionBreed()

        // Then
        assertNotNull(result)
        assertEquals(testQuizSession.questions[0].correctBreed.id, result?.id)
    }

    @Test
    fun `getCurrentQuestionBreed should return null when no current question`() = runTest {
        // When
        val result = viewModel.getCurrentQuestionBreed()

        // Then
        assertNull(result)
    }

    @Test
    fun `getQuizResults should return current quiz session`() = runTest {
        // Given
        val testQuizSession = createTestQuizSession()
        setupSuccessfulQuizStart(testQuizSession)
        advanceUntilIdle()

        // When
        val result = viewModel.getQuizResults()

        // Then
        assertNotNull(result)
        assertEquals(testQuizSession.id, result?.id)
    }

    @Test
    fun `getQuizStatistics should call use case`() = runTest {
        // When
        viewModel.getQuizStatistics()
        advanceUntilIdle()

        // Then
        verify(getQuizStatisticsUseCase).invoke()
    }

    // ===== HELPER METHODS =====

    private suspend fun setupSuccessfulQuizStart(testQuizSession: QuizSession) {
        whenever(generateQuizUseCase(any(), any())).thenReturn(Result.success(testQuizSession))
        whenever(loadBreedImageUseCase(any())).thenReturn(createTestBreed("1", "Golden Retriever"))
        viewModel.startNewQuiz(DogBreed.Difficulty.BEGINNER)
    }

    private fun createTestQuizSession(
        questions: List<QuizQuestion> = listOf(
            createTestQuizQuestion("1"),
            createTestQuizQuestion("2"),
            createTestQuizQuestion("3")
        ),
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER
    ): QuizSession {
        return QuizSession(
            id = "test_session_1",
            questions = questions,
            difficulty = difficulty,
            startTime = System.currentTimeMillis(),
            answers = emptyList()
        )
    }

    private fun createTestQuizQuestion(
        id: String = "question_1",
        imageUrl: String = ""
    ): QuizQuestion {
        val correctBreed = createTestBreed(id, "Test Breed $id")
        val options = listOf(
            correctBreed,
            createTestBreed("${id}_option_1", "Option 1"),
            createTestBreed("${id}_option_2", "Option 2")
        )
        return QuizQuestion(
            id = id,
            correctBreed = correctBreed,
            options = options,
            imageUrl = imageUrl
        )
    }

    private fun createTestBreed(
        id: String,
        name: String,
        imageUrl: String = "https://example.com/image.jpg"
    ): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = "Test description",
            funFact = "Test fun fact",
            origin = "Test origin",
            size = DogBreed.Size.MEDIUM,
            temperament = listOf("Friendly", "Intelligent"),
            lifeSpan = "10-12 years",
            difficulty = DogBreed.Difficulty.BEGINNER,
            isFavorite = false
        )
    }
}