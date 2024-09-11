package com.dogbreedquiz.app.domain.repository

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizSession

/**
 * Repository interface for quiz-related operations
 * Separates quiz logic from general breed data operations
 */
interface QuizRepository {
    
    /**
     * Generate a new quiz session with specified parameters
     * @param difficulty Difficulty level for the quiz
     * @param questionCount Number of questions to generate
     * @return Generated quiz session
     */
    suspend fun generateQuizSession(
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        questionCount: Int = 10
    ): QuizSession
    
    /**
     * Save quiz session results
     * @param quizSession Completed quiz session
     */
    suspend fun saveQuizSession(quizSession: QuizSession)
    
    /**
     * Get quiz history for analytics
     * @param limit Maximum number of sessions to return
     * @return List of completed quiz sessions
     */
    suspend fun getQuizHistory(limit: Int = 10): List<QuizSession>
    
    /**
     * Get quiz statistics
     * @return Quiz statistics summary
     */
    suspend fun getQuizStatistics(): QuizStatistics
}

/**
 * Data class for quiz statistics
 */
data class QuizStatistics(
    val totalQuizzes: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val averageAccuracy: Float,
    val averageScore: Int,
    val bestStreak: Int,
    val favoriteBreeds: List<String>
)