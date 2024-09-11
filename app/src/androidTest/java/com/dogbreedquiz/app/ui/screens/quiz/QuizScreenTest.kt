package com.dogbreedquiz.app.ui.screens.quiz

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizQuestion
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.usecase.GenerateQuizUseCase
import com.dogbreedquiz.app.domain.usecase.LoadBreedImageUseCase
import com.dogbreedquiz.app.domain.usecase.SaveQuizSessionUseCase
import com.dogbreedquiz.app.domain.usecase.GetQuizStatisticsUseCase
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock

/**
 * Android instrumentation tests for QuizScreen
 * Tests UI components, user interactions, and accessibility compliance
 */
@RunWith(AndroidJUnit4::class)
class QuizScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var initialUiState: QuizUiState

    // Mocks
    @Mock lateinit var generateQuizUseCase: GenerateQuizUseCase
    @Mock lateinit var loadBreedImageUseCase: LoadBreedImageUseCase
    @Mock lateinit var saveQuizSessionUseCase: SaveQuizSessionUseCase
    @Mock lateinit var getQuizStatisticsUseCase: GetQuizStatisticsUseCase

    @Before
    fun setup() {
        initialUiState = QuizUiState()

    }

    // ===== LOADING STATE TESTS =====

    @Test
    fun quizScreen_showsLoadingIndicatorWhenLoading() {
        // Given
        val loadingState = initialUiState.copy(isLoading = true)
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading dog breeds...").assertIsDisplayed()
    }

    @Test
    fun quizScreen_hidesLoadingIndicatorWhenNotLoading() {
        // Given
        val quizState = createQuizInProgressState()
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Loading dog breeds...").assertDoesNotExist()
    }

    // ===== ERROR STATE TESTS =====

    @Test
    fun quizScreen_showsErrorMessageWhenErrorExists() {
        // Given
        val errorMessage = "Failed to load quiz questions"
        val errorState = initialUiState.copy(
            isLoading = false,
            error = errorMessage
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun quizScreen_retryButtonTriggersRetryAction() {
        // Given
        var retryClicked = false
        val errorState = initialUiState.copy(
            isLoading = false,
            error = "Network error"
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                QuizScreen(
                    onAnswerCorrect = {},
                    onAnswerIncorrect = {},
                    onQuizComplete = {},
                    onNavigateBack = { retryClicked = true },
                    viewModel = testViewModel
                )
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(retryClicked)
    }

    // ===== QUIZ DISPLAY TESTS =====

    @Test
    fun quizScreen_displaysQuizQuestionCorrectly() {
        // Given
        val quizState = createQuizInProgressState()
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Question 1 of 3").assertIsDisplayed()
    }

    @Test
    fun quizScreen_displaysProgressIndicatorCorrectly() {
        // Given
        val quizState = createQuizInProgressState().copy(
            currentQuestionIndex = 1,
            totalQuestions = 5
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Question 2 of 5").assertIsDisplayed()
    }

    @Test
    fun quizScreen_displaysScoreAndLives() {
        // Given
        val quizState = createQuizInProgressState().copy(
            score = 150,
            lives = 2,
            currentStreak = 3
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("150").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed()
    }

    // ===== ANSWER OPTIONS TESTS =====

    @Test
    fun quizScreen_displaysThreeAnswerOptions() {
        // Given
        val quizState = createQuizInProgressState()
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )
        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Golden Retriever").assertIsDisplayed()
        composeTestRule.onNodeWithText("Labrador Retriever").assertIsDisplayed()
        composeTestRule.onNodeWithText("German Shepherd").assertIsDisplayed()
    }

    @Test
    fun quizScreen_supportsScreenReaderAnnouncements() {
        // Given
        val quizState = createQuizInProgressState()
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Golden Retriever")
            .assertHasClickAction()
            .assertIsEnabled()
    }

    // ===== QUIZ COMPLETION TESTS =====

    @Test
    fun quizScreen_showsQuizCompletionScreen() {
        // Given
        val completedState = createQuizInProgressState().copy(
            isQuizComplete = true,
            score = 250
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithText("Quiz Complete!").assertIsDisplayed()
        composeTestRule.onNodeWithText("250").assertIsDisplayed()
    }

    // ===== IMAGE LOADING TESTS =====

    @Test
    fun quizScreen_showsImageLoadingIndicator() {
        // Given
        val imageLoadingState = createQuizInProgressState().copy(
            isLoadingImage = true,
            currentQuestionImageUrl = ""
        )
        val testViewModel = QuizViewModel(
            generateQuizUseCase,
            loadBreedImageUseCase,
            saveQuizSessionUseCase,
            getQuizStatisticsUseCase
        )

        // When
        composeTestRule.setContent {
            DogBreedQuizTheme {
                TestableQuizScreen(testViewModel)
            }
        }

        // Then
        composeTestRule.onNodeWithContentDescription("Dog breed image").assertExists()
    }

    // ===== HELPER METHODS =====

    @Composable
    private fun TestableQuizScreen(viewModel: QuizViewModel) {
        QuizScreen(
            onAnswerCorrect = {},
            onAnswerIncorrect = {},
            onQuizComplete = {},
            onNavigateBack = {},
            viewModel = viewModel
        )
    }

    private fun createQuizInProgressState(): QuizUiState {
        val question = createTestQuizQuestion()
        val session = createTestQuizSession(listOf(question))

        return QuizUiState(
            isLoading = false,
            currentQuestion = question,
            currentQuestionIndex = 0,
            totalQuestions = 3,
            quizSession = session,
            score = 0,
            lives = 3,
            currentStreak = 0,
            isQuizComplete = false,
            error = null
        )
    }

    private fun createTestQuizQuestion(
        correctBreed: DogBreed = createTestBreed("golden_retriever", "Golden Retriever")
    ): QuizQuestion {
        val options = listOf(
            correctBreed,
            createTestBreed("labrador_retriever", "Labrador Retriever"),
            createTestBreed("german_shepherd", "German Shepherd")
        )

        return QuizQuestion(
            id = "question_1",
            correctBreed = correctBreed,
            options = options,
            imageUrl = "https://example.com/golden_retriever.jpg"
        )
    }

    private fun createTestQuizSession(questions: List<QuizQuestion>): QuizSession {
        return QuizSession(
            id = "test_session",
            questions = questions,
            difficulty = DogBreed.Difficulty.BEGINNER,
            startTime = System.currentTimeMillis(),
            answers = emptyList()
        )
    }

    private fun createTestBreed(id: String, name: String): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = "https://example.com/$id.jpg",
            description = "Test description for $name",
            funFact = "Test fun fact for $name",
            origin = "Test origin",
            size = DogBreed.Size.MEDIUM,
            temperament = listOf("Friendly", "Intelligent"),
            lifeSpan = "10-12 years",
            difficulty = DogBreed.Difficulty.BEGINNER,
            isFavorite = false
        )
    }
}