package com.dogbreedquiz.app.domain.model

/**
 * Domain model for a quiz question
 */
data class QuizQuestion(
    val id: String,
    val correctBreed: DogBreed,
    val options: List<DogBreed>,
    val imageUrl: String = correctBreed.imageUrl
) {
    /**
     * Check if the selected breed is correct
     */
    fun isCorrectAnswer(selectedBreed: DogBreed): Boolean = selectedBreed == correctBreed
    
    /**
     * Get shuffled options for display
     */
    fun getShuffledOptions(): List<DogBreed> = options.shuffled()
}

/**
 * Domain model for a quiz session
 */
data class QuizSession(
    val id: String,
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val answers: List<QuizAnswer> = emptyList(),
    val difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER
) {
    val isComplete: Boolean get() = currentQuestionIndex >= questions.size
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentQuestionIndex)
    val totalQuestions: Int get() = questions.size
    val accuracy: Float get() = if (answers.isEmpty()) 0f else correctAnswers.toFloat() / answers.size
    val duration: Long get() = System.currentTimeMillis() - startTime
    
    /**
     * Add an answer to the session
     */
    fun addAnswer(answer: QuizAnswer): QuizSession {
        val newAnswers = answers + answer
        val newCorrectAnswers = if (answer.isCorrect) correctAnswers + 1 else correctAnswers
        val newScore = if (answer.isCorrect) score + answer.pointsEarned else score
        
        return copy(
            answers = newAnswers,
            correctAnswers = newCorrectAnswers,
            score = newScore,
            currentQuestionIndex = currentQuestionIndex + 1
        )
    }
    
    /**
     * Get final results summary
     */
    fun getResultsSummary(): QuizResults {
        return QuizResults(
            sessionId = id,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            accuracy = accuracy,
            score = score,
            duration = duration,
            difficulty = difficulty,
            completedAt = System.currentTimeMillis()
        )
    }
}

/**
 * Domain model for a quiz answer
 */
data class QuizAnswer(
    val questionId: String,
    val selectedBreed: DogBreed,
    val correctBreed: DogBreed,
    val isCorrect: Boolean,
    val timeSpent: Long,
    val pointsEarned: Int,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        /**
         * Create a quiz answer with calculated points
         */
        fun create(
            questionId: String,
            selectedBreed: DogBreed,
            correctBreed: DogBreed,
            timeSpent: Long
        ): QuizAnswer {
            val isCorrect = selectedBreed == correctBreed
            val points = if (isCorrect) calculatePoints(timeSpent) else 0
            
            return QuizAnswer(
                questionId = questionId,
                selectedBreed = selectedBreed,
                correctBreed = correctBreed,
                isCorrect = isCorrect,
                timeSpent = timeSpent,
                pointsEarned = points
            )
        }
        
        private fun calculatePoints(timeSpent: Long): Int {
            val basePoints = 100
            val timeBonus = when {
                timeSpent < 3000 -> 50  // Under 3 seconds
                timeSpent < 5000 -> 30  // Under 5 seconds
                timeSpent < 10000 -> 10 // Under 10 seconds
                else -> 0
            }
            return basePoints + timeBonus
        }
    }
}

/**
 * Domain model for quiz results summary
 */
data class QuizResults(
    val sessionId: String,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val accuracy: Float,
    val score: Int,
    val duration: Long,
    val difficulty: DogBreed.Difficulty,
    val completedAt: Long
) {
    /**
     * Get performance rating based on accuracy
     */
    fun getPerformanceRating(): PerformanceRating {
        return when {
            accuracy >= 0.9f -> PerformanceRating.EXCELLENT
            accuracy >= 0.75f -> PerformanceRating.GOOD
            accuracy >= 0.5f -> PerformanceRating.FAIR
            else -> PerformanceRating.NEEDS_IMPROVEMENT
        }
    }
    
    enum class PerformanceRating {
        EXCELLENT, GOOD, FAIR, NEEDS_IMPROVEMENT
    }
}