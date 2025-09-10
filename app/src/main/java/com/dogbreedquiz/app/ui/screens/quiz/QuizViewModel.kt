
package com.dogbreedquiz.app.ui.screens.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dogbreedquiz.app.data.model.DogBreed
import com.dogbreedquiz.app.data.model.QuizAnswer
import com.dogbreedquiz.app.data.model.QuizQuestion
import com.dogbreedquiz.app.data.model.QuizSession
import com.dogbreedquiz.app.data.repository.DogBreedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

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

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val repository: DogBreedRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    private var questionStartTime: Long = 0L
    
    fun startNewQuiz(difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val quizSession = repository.generateQuizSession(difficulty, 10)
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
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load quiz: ${e.message}"
                )
            }
        }
    }
    
    fun selectAnswer(selectedBreed: DogBreed) {
        val currentState = _uiState.value
        val currentQuestion = currentState.currentQuestion ?: return
        
        if (currentState.showResult) return // Already answered
        
        val isCorrect = selectedBreed == currentQuestion.correctBreed
        val timeSpent = System.currentTimeMillis() - questionStartTime
        
        // Create quiz answer record
        val quizAnswer = QuizAnswer(
            questionId = currentQuestion.id,
            selectedBreed = selectedBreed,
            correctBreed = currentQuestion.correctBreed,
            isCorrect = isCorrect,
            timeSpent = timeSpent
        )
        
        // Update quiz session
        val updatedSession = currentState.quizSession?.let { session ->
            session.copy(
                answers = session.answers + quizAnswer,
                correctAnswers = if (isCorrect) session.correctAnswers + 1 else session.correctAnswers,
                score = if (isCorrect) session.score + calculatePoints(timeSpent) else session.score
            )
        }
        
        // Calculate new state
        val newScore = if (isCorrect) currentState.score + calculatePoints(timeSpent) else currentState.score
        val newLives = if (isCorrect) currentState.lives else maxOf(0, currentState.lives - 1)
        val newStreak = if (isCorrect) currentState.currentStreak + 1 else 0
        
        _uiState.value = currentState.copy(
            selectedAnswer = selectedBreed,
            showResult = true,
            isCorrect = isCorrect,
            score = newScore,
            lives = newLives,
            currentStreak = newStreak,
            quizSession = updatedSession
        )
    }
    
    fun nextQuestion() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val session = currentState.quizSession ?: return@launch
            
            // Add delay for smooth transition
            delay(300)
            
            val nextIndex = currentState.currentQuestionIndex + 1
            
            if (nextIndex >= session.totalQuestions || currentState.lives <= 0) {
                // Quiz complete
                _uiState.value = currentState.copy(
                    isQuizComplete = true
                )
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
                val breedWithImage = repository.loadBreedImage(correctBreed.id)
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
    
    fun retryQuiz() {
        startNewQuiz()
    }
    
    private fun calculatePoints(timeSpent: Long): Int {
        // Base points: 100
        // Time bonus: faster answers get more points
        val basePoints = 100
        val timeBonus = when {
            timeSpent < 3000 -> 50  // Under 3 seconds
            timeSpent < 5000 -> 30  // Under 5 seconds
            timeSpent < 10000 -> 10 // Under 10 seconds
            else -> 0
        }
        return basePoints + timeBonus
    }
    
    fun getCurrentQuestionBreed(): DogBreed? {
        return _uiState.value.currentQuestion?.correctBreed
    }
    
    fun getQuizResults(): QuizSession? {
        return _uiState.value.quizSession
    }
}