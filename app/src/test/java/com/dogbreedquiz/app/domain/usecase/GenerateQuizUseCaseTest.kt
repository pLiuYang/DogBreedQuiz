package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for GenerateQuizUseCase
 * Tests the business logic for generating quiz sessions
 */
class GenerateQuizUseCaseTest {
    
    @Mock
    private lateinit var breedRepository: DogBreedRepository
    
    private lateinit var useCase: GenerateQuizUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GenerateQuizUseCase(breedRepository)
    }
    
    @Test
    fun `invoke should generate quiz session with correct number of questions`() = runTest {
        // Given
        val availableBreeds = createTestBreeds(10)
        whenever(breedRepository.getAllBreeds()).thenReturn(availableBreeds)
        
        // When
        val result = useCase.invoke(DogBreed.Difficulty.BEGINNER, 5)
        
        // Then
        assertTrue(result.isSuccess)
        val quizSession = result.getOrThrow()
        assertEquals(5, quizSession.questions.size)
        assertEquals(DogBreed.Difficulty.BEGINNER, quizSession.difficulty)
    }
    
    @Test
    fun `invoke should generate quiz with 4 options per question`() = runTest {
        // Given
        val availableBreeds = createTestBreeds(10)
        whenever(breedRepository.getAllBreeds()).thenReturn(availableBreeds)
        
        // When
        val result = useCase.invoke(DogBreed.Difficulty.BEGINNER, 4)
        
        // Then
        assertTrue(result.isSuccess)
        val quizSession = result.getOrThrow()
        quizSession.questions.forEach { question ->
            assertEquals(4, question.options.size)
            assertTrue(question.options.contains(question.correctBreed))
        }
    }
    
    @Test
    fun `invoke should fail when not enough breeds available`() = runTest {
        // Given - only 2 breeds available, but need 3 for quiz options
        val availableBreeds = createTestBreeds(2)
        whenever(breedRepository.getAllBreeds()).thenReturn(availableBreeds)
        
        // When
        val result = useCase.invoke(DogBreed.Difficulty.BEGINNER, 1)
        
        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalStateException)
    }
    
    @Test
    fun `invoke should generate unique session and question IDs`() = runTest {
        // Given
        val availableBreeds = createTestBreeds(10)
        whenever(breedRepository.getAllBreeds()).thenReturn(availableBreeds)
        
        // When
        val result1 = useCase.invoke(DogBreed.Difficulty.BEGINNER, 3)
        val result2 = useCase.invoke(DogBreed.Difficulty.BEGINNER, 3)
        
        // Then
        assertTrue(result1.isSuccess)
        assertTrue(result2.isSuccess)
        
        val session1 = result1.getOrThrow()
        val session2 = result2.getOrThrow()
        
        // Sessions should have unique IDs
        assertTrue(session1.id != session2.id)
        
        // Questions should have unique IDs within each session
        val questionIds1 = session1.questions.map { it.id }
        val questionIds2 = session2.questions.map { it.id }
        
        assertEquals(questionIds1.size, questionIds1.toSet().size) // No duplicates in session 1
        assertEquals(questionIds2.size, questionIds2.toSet().size) // No duplicates in session 2
    }
    
    @Test
    fun `invoke should handle different difficulty levels`() = runTest {
        // Given
        val beginnerBreeds = createTestBreeds(5, DogBreed.Difficulty.BEGINNER)
        val intermediateBreeds = createTestBreeds(3, DogBreed.Difficulty.INTERMEDIATE)
        val allBreeds = beginnerBreeds + intermediateBreeds
        
        whenever(breedRepository.getAllBreeds()).thenReturn(allBreeds)
        
        // When
        val beginnerResult = useCase.invoke(DogBreed.Difficulty.BEGINNER, 2)
        val intermediateResult = useCase.invoke(DogBreed.Difficulty.INTERMEDIATE, 2)
        
        // Then
        assertTrue(beginnerResult.isSuccess)
        assertTrue(intermediateResult.isSuccess)
        
        val beginnerSession = beginnerResult.getOrThrow()
        val intermediateSession = intermediateResult.getOrThrow()
        
        assertEquals(DogBreed.Difficulty.BEGINNER, beginnerSession.difficulty)
        assertEquals(DogBreed.Difficulty.INTERMEDIATE, intermediateSession.difficulty)
    }
    
    @Test
    fun `invoke should limit questions to available breeds`() = runTest {
        // Given - only 6 breeds available
        val availableBreeds = createTestBreeds(6)
        whenever(breedRepository.getAllBreeds()).thenReturn(availableBreeds)
        
        // When - request 10 questions but only 6 breeds available
        val result = useCase.invoke(DogBreed.Difficulty.BEGINNER, 10)
        
        // Then - should generate maximum possible questions (6)
        assertTrue(result.isSuccess)
        val quizSession = result.getOrThrow()
        assertEquals(6, quizSession.questions.size)
    }
    
    private fun createTestBreeds(
        count: Int, 
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER
    ): List<DogBreed> {
        return (1..count).map { index ->
            DogBreed(
                id = "breed_$index",
                name = "Test Breed $index",
                imageUrl = "https://example.com/image$index.jpg",
                description = "Test description $index",
                funFact = "Test fun fact $index",
                origin = "Test origin $index",
                size = DogBreed.Size.MEDIUM,
                temperament = listOf("Friendly", "Intelligent"),
                lifeSpan = "10-12 years",
                difficulty = difficulty,
                isFavorite = false
            )
        }
    }
}