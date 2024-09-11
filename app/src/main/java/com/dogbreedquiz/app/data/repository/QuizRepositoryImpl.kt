package com.dogbreedquiz.app.data.repository

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import com.dogbreedquiz.app.domain.repository.QuizRepository
import com.dogbreedquiz.app.domain.repository.QuizStatistics
import com.dogbreedquiz.app.domain.usecase.GenerateQuizUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of QuizRepository interface
 * Handles quiz generation, storage, and statistics
 */
@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val generateQuizUseCase: GenerateQuizUseCase
) : QuizRepository {
    
    // In-memory storage for quiz sessions (in production, use Room database)
    private val quizHistory = mutableListOf<QuizSession>()
    private val quizStats = mutableMapOf<String, Int>()
    
    override suspend fun generateQuizSession(
        difficulty: DogBreed.Difficulty,
        questionCount: Int
    ): QuizSession {
        val result = generateQuizUseCase(difficulty, questionCount)
        return result.getOrThrow()
    }
    
    override suspend fun saveQuizSession(quizSession: QuizSession) {
        // Only save completed sessions
        if (quizSession.isComplete) {
            quizHistory.add(quizSession)
            
            // Update statistics
            updateQuizStatistics(quizSession)
            
            // Keep only last 50 sessions to manage memory
            if (quizHistory.size > 50) {
                quizHistory.removeAt(0)
            }
        }
    }
    
    override suspend fun getQuizHistory(limit: Int): List<QuizSession> {
        return quizHistory
            .sortedByDescending { it.startTime }
            .take(limit)
    }
    
    override suspend fun getQuizStatistics(): QuizStatistics {
        val totalQuizzes = quizHistory.size
        val totalQuestions = quizHistory.sumOf { it.totalQuestions }
        val correctAnswers = quizHistory.sumOf { it.correctAnswers }
        val averageAccuracy = if (totalQuestions > 0) {
            correctAnswers.toFloat() / totalQuestions
        } else 0f
        val averageScore = if (totalQuizzes > 0) {
            quizHistory.sumOf { it.score } / totalQuizzes
        } else 0
        val bestStreak = calculateBestStreak()
        val favoriteBreeds = getFavoriteBreeds()
        
        return QuizStatistics(
            totalQuizzes = totalQuizzes,
            totalQuestions = totalQuestions,
            correctAnswers = correctAnswers,
            averageAccuracy = averageAccuracy,
            averageScore = averageScore,
            bestStreak = bestStreak,
            favoriteBreeds = favoriteBreeds
        )
    }
    
    /**
     * Update quiz statistics based on completed session
     */
    private fun updateQuizStatistics(session: QuizSession) {
        // Track breed performance
        session.answers.forEach { answer ->
            val breedId = answer.correctBreed.id
            if (answer.isCorrect) {
                quizStats[breedId] = (quizStats[breedId] ?: 0) + 1
            }
        }
    }
    
    /**
     * Calculate the best streak across all quiz sessions
     */
    private fun calculateBestStreak(): Int {
        var maxStreak = 0
        var currentStreak = 0
        
        quizHistory.forEach { session ->
            session.answers.forEach { answer ->
                if (answer.isCorrect) {
                    currentStreak++
                    maxStreak = maxOf(maxStreak, currentStreak)
                } else {
                    currentStreak = 0
                }
            }
        }
        
        return maxStreak
    }
    
    /**
     * Get favorite breeds based on quiz performance
     */
    private fun getFavoriteBreeds(): List<String> {
        return quizStats
            .toList()
            .sortedByDescending { it.second }
            .take(5)
            .map { it.first }
    }
}