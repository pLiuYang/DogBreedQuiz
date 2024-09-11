package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for GetAllBreedsUseCase
 * Tests the business logic for retrieving all dog breeds
 */
class GetAllBreedsUseCaseTest {
    
    @Mock
    private lateinit var repository: DogBreedRepository
    
    private lateinit var useCase: GetAllBreedsUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = GetAllBreedsUseCase(repository)
    }
    
    @Test
    fun `invoke should return all breeds from repository`() = runTest {
        // Given
        val expectedBreeds = listOf(
            createTestBreed("1", "Golden Retriever"),
            createTestBreed("2", "Labrador Retriever")
        )
        whenever(repository.getAllBreeds(false)).thenReturn(expectedBreeds)
        
        // When
        val result = useCase.invoke(false)
        
        // Then
        assertEquals(expectedBreeds, result)
        verify(repository).getAllBreeds(false)
    }
    
    @Test
    fun `invoke with force refresh should call repository with force refresh`() = runTest {
        // Given
        val expectedBreeds = listOf(createTestBreed("1", "Golden Retriever"))
        whenever(repository.getAllBreeds(true)).thenReturn(expectedBreeds)
        
        // When
        val result = useCase.invoke(true)
        
        // Then
        assertEquals(expectedBreeds, result)
        verify(repository).getAllBreeds(true)
    }
    
    @Test
    fun `asFlow should return breeds flow from repository`() = runTest {
        // Given
        val expectedBreeds = listOf(createTestBreed("1", "Golden Retriever"))
        val expectedFlow = flowOf(expectedBreeds)
        whenever(repository.getAllBreedsFlow()).thenReturn(expectedFlow)
        
        // When
        val resultFlow = useCase.asFlow()
        
        // Then
        assertEquals(expectedFlow, resultFlow)
        verify(repository).getAllBreedsFlow()
    }
    
    @Test
    fun `invoke should return empty list when repository returns empty list`() = runTest {
        // Given
        whenever(repository.getAllBreeds(false)).thenReturn(emptyList())
        
        // When
        val result = useCase.invoke(false)
        
        // Then
        assertTrue(result.isEmpty())
        verify(repository).getAllBreeds(false)
    }
    
    private fun createTestBreed(id: String, name: String): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = "https://example.com/image.jpg",
            description = "Test description",
            funFact = "Test fun fact",
            origin = "Test origin",
            size = DogBreed.Size.MEDIUM,
            temperament = listOf("Friendly", "Intelligent"),
            lifeSpan = "10-12 years",
            difficulty = DogBreed.Difficulty.BEGINNER,
            isFavorite = false
        )
    }
}