package com.dogbreedquiz.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizAnswer
import com.dogbreedquiz.app.domain.model.QuizQuestion
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.usecase.GenerateQuizUseCase
import com.dogbreedquiz.app.domain.usecase.LoadBreedImageUseCase
import com.dogbreedquiz.app.domain.usecase.SaveQuizSessionUseCase
import com.dogbreedquiz.app.domain.usecase.GetQuizStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the quiz screen
 * Represents all the state needed for the quiz UI
 */
data class QuizUiState(
    val isLoading: Boolean = false,
    val currentQuestion: QuizQuestion? = null,
    val currentQuestionIndex: Int = 0,
    val totalQuestions: Int = 0,
    val selectedAnswer: DogBreed? = null,
    val showResult: Boolean = false,
    val isCorrect: Boolean = false,
    val score: Int = 0,
    val lives: Int = 3,
    val currentStreak: Int = 0,
    val isQuizComplete: Boolean = false,
    val quizSession: QuizSession? = null,
    val error: String? = null,
    val isLoadingImage: Boolean = false,
    val currentQuestionImageUrl: String = ""
)

/**
 * ViewModel for the quiz screen using clean architecture principles
 * Uses use cases to handle business logic and maintains UI state
 */
@HiltViewModel
class QuizViewModel @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase,
    private val loadBreedImageUseCase: LoadBreedImageUseCase,
    private val saveQuizSessionUseCase: SaveQuizSessionUseCase,
    private val getQuizStatisticsUseCase: GetQuizStatisticsUseCase
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    private var questionStartTime: Long = 0L
    
    /**
     * Start a new quiz with the specified difficulty
     */
    fun startNewQuiz(difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val result = generateQuizUseCase(difficulty, 10)
                
                if (result.isSuccess) {
                    val quizSession = result.getOrThrow()
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        quizSession = quizSession,
                        currentQuestion = quizSession.currentQuestion,
                        currentQuestionIndex = 0,
                        totalQuestions = quizSession.totalQuestions,
                        selectedAnswer = null,
                        showResult = false,
                        isCorrect = false,
                        score = 0,
                        lives = 3,
                        currentStreak = 0,
                        isQuizComplete = false
                    )
                    questionStartTime = System.currentTimeMillis()
                    
                    // Load image for the first question
                    loadCurrentQuestionImage()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to generate quiz: ${result.exceptionOrNull()?.message}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load quiz: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Select an answer for the current question
     */
    fun selectAnswer(selectedBreed: DogBreed) {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion ?: return
        
        if (currentState.showResult) return // Already answered
        
        val timeSpent = System.currentTimeMillis() - questionStartTime
        
        // Create quiz answer using domain model factory method
        val quizAnswer = QuizAnswer.create(
            questionId = currentQuestion.id,
            selectedBreed = selectedBreed,
            correctBreed = currentQuestion.correctBreed,
            timeSpent = timeSpent
        )
        
        // Update quiz session with the new answer
        val updatedSession = currentState.quizSession?.addAnswer(quizAnswer)
        
        // Calculate new UI state
        val newScore = if (quizAnswer.isCorrect) {
            currentState.score + quizAnswer.pointsEarned
        } else {
            currentState.score
        }
        val newLives = if (quizAnswer.isCorrect) currentState.lives else maxOf(0, currentState.lives - 1)
        val newStreak = if (quizAnswer.isCorrect) currentState.currentStreak + 1 else 0
        
        _uiState.value = currentState.copy(
            selectedAnswer = selectedBreed,
            showResult = true,
            isCorrect = quizAnswer.isCorrect,
            score = newScore,
            lives = newLives,
            currentStreak = newStreak,
            quizSession = updatedSession
        )
    }
    
    /**
     * Move to the next question or complete the quiz
     */
    fun nextQuestion() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val session = currentState.quizSession ?: return@launch
            
            // Add delay for smooth transition
            delay(300)
            
            val nextIndex = currentState.currentQuestionIndex + 1
            
            if (nextIndex >= session.totalQuestions || currentState.lives <= 0) {
                // Quiz complete - save the session
                completeQuiz(session)
            } else {
                // Move to next question
                val nextQuestion = session.questions.getOrNull(nextIndex)
                _uiState.value = currentState.copy(
                    currentQuestion = nextQuestion,
                    currentQuestionIndex = nextIndex,
                    selectedAnswer = null,
                    showResult = false,
                    isCorrect = false,
                    currentQuestionImageUrl = "",
                    isLoadingImage = false
                )
                questionStartTime = System.currentTimeMillis()
                
                // Load image for the new question
                loadCurrentQuestionImage()
            }
        }
    }
    
    /**
     * Complete the quiz and save results
     */
    private suspend fun completeQuiz(session: QuizSession) {
        val saveResult = saveQuizSessionUseCase(session)
        
        if (saveResult.isSuccess) {
            _uiState.value = _uiState.value.copy(
                isQuizComplete = true
            )
        } else {
            // Even if saving fails, mark quiz as complete
            _uiState.value = _uiState.value.copy(
                isQuizComplete = true,
                error = "Quiz completed but failed to save results: ${saveResult.exceptionOrNull()?.message}"
            )
        }
    }
    
    /**
     * Load image for the current question
     */
    private fun loadCurrentQuestionImage() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentQuestion = currentState.currentQuestion ?: return@launch
            val correctBreed = currentQuestion.correctBreed
            
            // Skip if image already loaded
            if (currentQuestion.imageUrl.isNotEmpty()) {
                _uiState.value = currentState.copy(
                    currentQuestionImageUrl = currentQuestion.imageUrl,
                    isLoadingImage = false
                )
                return@launch
            }
            
            _uiState.value = currentState.copy(isLoadingImage = true)
            
            try {
                val breedWithImage = loadBreedImageUseCase(correctBreed.id)
                val imageUrl = breedWithImage?.imageUrl ?: ""
                
                // Update the current question with the loaded image
                val updatedQuestion = currentQuestion.copy(imageUrl = imageUrl)
                val updatedSession = currentState.quizSession?.let { session ->
                    val updatedQuestions = session.questions.toMutableList()
                    val questionIndex = updatedQuestions.indexOfFirst { it.id == currentQuestion.id }
                    if (questionIndex != -1) {
                        updatedQuestions[questionIndex] = updatedQuestion
                        session.copy(questions = updatedQuestions)
                    } else {
                        session
                    }
                }
                
                _uiState.value = currentState.copy(
                    currentQuestion = updatedQuestion,
                    quizSession = updatedSession,
                    currentQuestionImageUrl = imageUrl,
                    isLoadingImage = false
                )
            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isLoadingImage = false,
                    currentQuestionImageUrl = ""
                )
            }
        }
    }
    
    /**
     * Retry the quiz (start a new one)
     */
    fun retryQuiz() {
        val currentDifficulty = _uiState.value.quizSession?.difficulty ?: DogBreed.Difficulty.BEGINNER
        startNewQuiz(currentDifficulty)
    }
    
    /**
     * Clear any error messages
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * Get the current question's correct breed
     */
    fun getCurrentQuestionBreed(): DogBreed? {
        return _uiState.value.currentQuestion?.correctBreed
    }
    
    /**
     * Get the final quiz results
     */
    fun getQuizResults(): QuizSession? {
        return _uiState.value.quizSession
    }
    
    /**
     * Get quiz statistics for display
     */
    fun getQuizStatistics() {
        viewModelScope.launch {
            val stats = getQuizStatisticsUseCase()
            // You could expose this through another StateFlow if needed for UI
            // For now, this is a passthrough call that maintains clean architecture
        }
    }
}