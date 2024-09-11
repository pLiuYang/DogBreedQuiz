package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.repository.QuizRepository
import com.dogbreedquiz.app.domain.repository.QuizStatistics
import javax.inject.Inject

/**
 * Use case for retrieving quiz statistics
 * Handles the business logic for fetching and calculating quiz performance metrics
 */
class GetQuizStatisticsUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    
    /**
     * Get comprehensive quiz statistics
     * @return Quiz statistics or null if no data available
     */
    suspend operator fun invoke(): QuizStatistics? {
        return try {
            quizRepository.getQuizStatistics()
        } catch (e: Exception) {
            // Log error or handle as needed
            null
        }
    }
}