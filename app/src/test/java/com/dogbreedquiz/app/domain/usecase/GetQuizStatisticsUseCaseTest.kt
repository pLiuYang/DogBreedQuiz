
package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.repository.QuizStatistics
import com.dogbreedquiz.app.domain.repository.QuizRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for GetQuizStatisticsUseCase
 * Tests the business logic for retrieving quiz performance statistics
 */
class GetQuizStatisticsUseCaseTest {
    
    @Mock
    private lateinit var repository: QuizRepository
    
    private lateinit var useCase: GetQuizStatisticsUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetQuizStatisticsUseCase(repository)
    }
    
    @Test
    fun `invoke should return statistics from repository`() = runTest {
        // Given
        val expectedStats = createTestStatistics(
            totalQuizzes = 10,
            totalQuestions = 50,
            correctAnswers = 35,
            averageAccuracy = 0.7f,
            averageScore = 70,
            bestStreak = 8
        )
        whenever(repository.getQuizStatistics()).thenReturn(expectedStats)
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertEquals(expectedStats, result)
        verify(repository).getQuizStatistics()
    }
    
    @Test
    fun `invoke should return null when repository throws exception`() = runTest {
        // Given
        whenever(repository.getQuizStatistics()).thenThrow(RuntimeException("Database error"))
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertNull(result)
        verify(repository).getQuizStatistics()
    }
    
    @Test
    fun `invoke should return empty statistics when no quizzes completed`() = runTest {
        // Given
        val emptyStats = createTestStatistics(
            totalQuizzes = 0,
            totalQuestions = 0,
            correctAnswers = 0,
            averageAccuracy = 0.0f,
            averageScore = 0,
            bestStreak = 0
        )
        whenever(repository.getQuizStatistics()).thenReturn(emptyStats)
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertEquals(emptyStats, result)
        assertEquals(0, result?.totalQuizzes)
        assertEquals(0, result?.totalQuestions)
        assertEquals(0, result?.correctAnswers)
        assertEquals(0.0f, result?.averageAccuracy)
        assertEquals(0, result?.averageScore)
        assertEquals(0, result?.bestStreak)
        verify(repository).getQuizStatistics()
    }
    
    @Test
    fun `invoke should return perfect statistics for all correct answers`() = runTest {
        // Given
        val perfectStats = createTestStatistics(
            totalQuizzes = 5,
            totalQuestions = 25,
            correctAnswers = 25,
            averageAccuracy = 1.0f,
            averageScore = 150,
            bestStreak = 15
        )
        whenever(repository.getQuizStatistics()).thenReturn(perfectStats)
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertEquals(perfectStats, result)
        assertEquals(1.0f, result?.averageAccuracy)
        assertEquals(25, result?.correctAnswers)
        assertEquals(25, result?.totalQuestions)
        verify(repository).getQuizStatistics()
    }
    
    @Test
    fun `invoke should return statistics with favorite breeds`() = runTest {
        // Given
        val favoriteBreeds = listOf("Golden Retriever", "Labrador Retriever", "German Shepherd")
        val statsWithFavorites = createTestStatistics(
            totalQuizzes = 15,
            totalQuestions = 75,
            correctAnswers = 45,
            averageAccuracy = 0.6f,
            averageScore = 90,
            bestStreak = 12,
            favoriteBreeds = favoriteBreeds
        )
        whenever(repository.getQuizStatistics()).thenReturn(statsWithFavorites)
        
        // When
        val result = useCase.invoke()
        
        // Then
        assertEquals(statsWithFavorites, result)
        assertEquals(favoriteBreeds, result?.favoriteBreeds)
        verify(repository).getQuizStatistics()
    }
    
    @Test
    fun `invoke should return consistent statistics across multiple calls`() = runTest {
        // Given
        val consistentStats = createTestStatistics(
            totalQuizzes = 25,
            totalQuestions = 125,
            correctAnswers = 87,
            averageAccuracy = 0.696f,
            averageScore = 104,
            bestStreak = 14
        )
        whenever(repository.getQuizStatistics()).thenReturn(consistentStats)
        
        // When
        val result1 = useCase.invoke()
        val result2 = useCase.invoke()
        val result3 = useCase.invoke()
        
        // Then
        assertEquals(result1, result2)
        assertEquals(result2, result3)
        assertEquals(result1, result3)
        verify(repository, org.mockito.kotlin.times(3)).getQuizStatistics()
    }
    
    // ===== HELPER METHODS =====
    
    private fun createTestStatistics(
        totalQuizzes: Int = 0,
        totalQuestions: Int = 0,
        correctAnswers: Int = 0,
        averageAccuracy: Float = 0.0f,
        averageScore: Int = 0,
        bestStreak: Int = 0,
        favoriteBreeds: List<String> = emptyList()
    ): QuizStatistics {
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
}
