package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.repository.QuizRepository
import javax.inject.Inject

/**
 * Use case for saving quiz sessions
 * Handles the business logic for persisting completed quiz sessions
 */
class SaveQuizSessionUseCase @Inject constructor(
    private val quizRepository: QuizRepository
) {
    
    /**
     * Save a completed quiz session
     * @param session The quiz session to save
     * @return Result indicating success or failure
     */
    suspend operator fun invoke(session: QuizSession): Result<Unit> {
        return try {
            quizRepository.saveQuizSession(session)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}