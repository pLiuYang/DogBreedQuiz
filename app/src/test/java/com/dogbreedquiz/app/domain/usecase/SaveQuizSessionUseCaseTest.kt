package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizAnswer
import com.dogbreedquiz.app.domain.model.QuizQuestion
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.repository.QuizRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for SaveQuizSessionUseCase
 * Tests the business logic for persisting quiz session data with proper error handling
 */
class SaveQuizSessionUseCaseTest {
    
    @Mock
    private lateinit var repository: QuizRepository
    
    private lateinit var useCase: SaveQuizSessionUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = SaveQuizSessionUseCase(repository)
    }
    
    @Test
    fun `invoke should successfully save complete quiz session`() = runTest {
        // Given
        val quizSession = createCompleteQuizSession()
        whenever(repository.saveQuizSession(quizSession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(quizSession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(quizSession)
    }
    
    @Test
    fun `invoke should return failure when repository throws exception`() = runTest {
        // Given
        val quizSession = createCompleteQuizSession()
        val exception = RuntimeException("Database error")
        whenever(repository.saveQuizSession(quizSession)).thenThrow(exception)
        
        // When
        val result = useCase.invoke(quizSession)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        verify(repository).saveQuizSession(quizSession)
    }
    
    @Test
    fun `invoke should save session with no answers`() = runTest {
        // Given
        val emptySession = createQuizSession(answers = emptyList())
        whenever(repository.saveQuizSession(emptySession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(emptySession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(emptySession)
    }
    
    @Test
    fun `invoke should save session with partial answers`() = runTest {
        // Given
        val partialSession = createQuizSession(
            questions = createTestQuestions(5),
            answers = listOf(createTestAnswer("1", true), createTestAnswer("2", false))
        )
        whenever(repository.saveQuizSession(partialSession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(partialSession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(partialSession)
    }
    
    @Test
    fun `invoke should save session with all correct answers`() = runTest {
        // Given
        val perfectSession = createQuizSession(
            questions = createTestQuestions(3),
            answers = listOf(
                createTestAnswer("1", true),
                createTestAnswer("2", true),
                createTestAnswer("3", true)
            )
        )
        whenever(repository.saveQuizSession(perfectSession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(perfectSession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(perfectSession)
    }
    
    @Test
    fun `invoke should save session with all incorrect answers`() = runTest {
        // Given
        val failedSession = createQuizSession(
            questions = createTestQuestions(3),
            answers = listOf(
                createTestAnswer("1", false),
                createTestAnswer("2", false),
                createTestAnswer("3", false)
            )
        )
        whenever(repository.saveQuizSession(failedSession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(failedSession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(failedSession)
    }
    
    @Test
    fun `invoke should save session with different difficulty levels`() = runTest {
        // Given
        val advancedSession = createQuizSession(difficulty = DogBreed.Difficulty.ADVANCED)
        whenever(repository.saveQuizSession(advancedSession)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(advancedSession)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(advancedSession)
    }
    
    @Test
    fun `invoke should save session with varying answer times`() = runTest {
        // Given
        val sessionWithVariedTimes = createQuizSession(
            questions = createTestQuestions(3),
            answers = listOf(
                createTestAnswer("1", true, timeSpent = 1500L),  // Fast answer
                createTestAnswer("2", false, timeSpent = 5000L), // Slow answer
                createTestAnswer("3", true, timeSpent = 3000L)   // Medium answer
            )
        )
        whenever(repository.saveQuizSession(sessionWithVariedTimes)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(sessionWithVariedTimes)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(sessionWithVariedTimes)
    }
    
    @Test
    fun `invoke should handle concurrent save attempts`() = runTest {
        // Given
        val session1 = createQuizSession(id = "session_1")
        val session2 = createQuizSession(id = "session_2")
        whenever(repository.saveQuizSession(session1)).thenReturn(Unit)
        whenever(repository.saveQuizSession(session2)).thenReturn(Unit)
        
        // When
        val result1 = useCase.invoke(session1)
        val result2 = useCase.invoke(session2)
        
        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        verify(repository).saveQuizSession(session1)
        verify(repository).saveQuizSession(session2)
    }
    
    @Test
    fun `invoke should preserve all session metadata`() = runTest {
        // Given
        val sessionWithMetadata = QuizSession(
            id = "detailed_session",
            questions = createTestQuestions(2),
            difficulty = DogBreed.Difficulty.EXPERT,
            startTime = 1640995200000L, // Specific timestamp
            answers = listOf(createTestAnswer("1", true))
        )
        whenever(repository.saveQuizSession(sessionWithMetadata)).thenReturn(Unit)
        
        // When
        val result = useCase.invoke(sessionWithMetadata)
        
        // Then
        assertTrue(result.isSuccess)
        verify(repository).saveQuizSession(sessionWithMetadata)
    }
    
    @Test
    fun `invoke should handle database constraint violations`() = runTest {
        // Given
        val quizSession = createCompleteQuizSession()
        val constraintException = RuntimeException("UNIQUE constraint failed")
        whenever(repository.saveQuizSession(quizSession)).thenThrow(constraintException)
        
        // When
        val result = useCase.invoke(quizSession)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(constraintException, result.exceptionOrNull())
        verify(repository).saveQuizSession(quizSession)
    }
    
    @Test
    fun `invoke should handle network timeout exceptions`() = runTest {
        // Given
        val quizSession = createCompleteQuizSession()
        val timeoutException = RuntimeException("Network timeout")
        whenever(repository.saveQuizSession(quizSession)).thenThrow(timeoutException)
        
        // When
        val result = useCase.invoke(quizSession)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(timeoutException, result.exceptionOrNull())
        verify(repository).saveQuizSession(quizSession)
    }
    
    @Test
    fun `invoke should handle storage full exceptions`() = runTest {
        // Given
        val quizSession = createCompleteQuizSession()
        val storageException = RuntimeException("Storage full")
        whenever(repository.saveQuizSession(quizSession)).thenThrow(storageException)
        
        // When
        val result = useCase.invoke(quizSession)
        
        // Then
        assertTrue(result.isFailure)
        assertEquals(storageException, result.exceptionOrNull())
        verify(repository).saveQuizSession(quizSession)
    }
    
    // ===== HELPER METHODS =====
    
    private fun createCompleteQuizSession(): QuizSession {
        val questions = createTestQuestions(3)
        val answers = questions.mapIndexed { index, question ->
            createTestAnswer(question.id, index % 2 == 0) // Alternate correct/incorrect
        }
        return createQuizSession(questions = questions, answers = answers)
    }
    
    private fun createQuizSession(
        id: String = "test_session_1",
        questions: List<QuizQuestion> = createTestQuestions(3),
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        answers: List<QuizAnswer> = emptyList()
    ): QuizSession {
        return QuizSession(
            id = id,
            questions = questions,
            difficulty = difficulty,
            startTime = System.currentTimeMillis() - 60000L, // 1 minute ago
            answers = answers
        )
    }
    
    private fun createTestQuestions(count: Int): List<QuizQuestion> {
        return (1..count).map { index ->
            val correctBreed = createTestBreed("breed_$index", "Test Breed $index")
            QuizQuestion(
                id = "question_$index",
                correctBreed = correctBreed,
                options = listOf(
                    correctBreed,
                    createTestBreed("option_${index}_1", "Option ${index}_1"),
                    createTestBreed("option_${index}_2", "Option ${index}_2")
                ),
                imageUrl = "https://example.com/image_$index.jpg"
            )
        }
    }
    
    private fun createTestAnswer(
        questionId: String,
        isCorrect: Boolean,
        timeSpent: Long = 3000L
    ): QuizAnswer {
        val correctBreed = createTestBreed("correct_$questionId", "Correct Breed")
        val selectedBreed = if (isCorrect) {
            correctBreed
        } else {
            createTestBreed("wrong_$questionId", "Wrong Breed")
        }
        
        return QuizAnswer.create(
            questionId = questionId,
            selectedBreed = selectedBreed,
            correctBreed = correctBreed,
            timeSpent = timeSpent
        )
    }
    
    private fun createTestBreed(
        id: String,
        name: String,
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER
    ): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = "https://example.com/image.jpg",
            description = "Test description for $name",
            funFact = "Test fun fact for $name",
            origin = "Test origin",
            size = DogBreed.Size.MEDIUM,
            temperament = listOf("Friendly", "Intelligent"),
            lifeSpan = "10-12 years",
            difficulty = difficulty,
            isFavorite = false
        )
    }
}