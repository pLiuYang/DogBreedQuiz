package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
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
 * Unit tests for LoadBreedImageUseCase
 * Tests the business logic for loading breed images with proper error handling
 */
class LoadBreedImageUseCaseTest {
    
    @Mock
    private lateinit var repository: DogBreedRepository
    
    private lateinit var useCase: LoadBreedImageUseCase
    
    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        useCase = LoadBreedImageUseCase(repository)
    }
    
    @Test
    fun `invoke should return breed with image when loading succeeds`() = runTest {
        // Given
        val breedId = "golden_retriever"
        val expectedBreed = createTestBreed(breedId, "Golden Retriever", "https://example.com/image.jpg")
        whenever(repository.loadBreedImage(breedId)).thenReturn(expectedBreed)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(expectedBreed, result)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should return null when breed not found`() = runTest {
        // Given
        val breedId = "nonexistent_breed"
        whenever(repository.loadBreedImage(breedId)).thenReturn(null)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertNull(result)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should return breed without image when image loading fails`() = runTest {
        // Given
        val breedId = "golden_retriever"
        val breedWithoutImage = createTestBreed(breedId, "Golden Retriever", "")
        whenever(repository.loadBreedImage(breedId)).thenReturn(breedWithoutImage)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(breedWithoutImage, result)
        assertEquals("", result?.imageUrl)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should handle repository exception gracefully`() = runTest {
        // Given
        val breedId = "golden_retriever"
        whenever(repository.loadBreedImage(breedId)).thenThrow(RuntimeException("Network error"))
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertNull(result)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should handle empty breed id`() = runTest {
        // Given
        val emptyBreedId = ""
        whenever(repository.loadBreedImage(emptyBreedId)).thenReturn(null)
        
        // When
        val result = useCase.invoke(emptyBreedId)
        
        // Then
        assertNull(result)
        verify(repository).loadBreedImage(emptyBreedId)
    }
    
    @Test
    fun `invoke should handle null image URL in breed`() = runTest {
        // Given
        val breedId = "test_breed"
        val breedWithNullImage = createTestBreed(breedId, "Test Breed", "")
        whenever(repository.loadBreedImage(breedId)).thenReturn(breedWithNullImage)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(breedWithNullImage, result)
        assertEquals("", result?.imageUrl)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should return breed with valid image URL`() = runTest {
        // Given
        val breedId = "labrador_retriever"
        val imageUrl = "https://images.dog.ceo/breeds/retriever-labrador/n02099712_100.jpg"
        val breedWithImage = createTestBreed(breedId, "Labrador Retriever", imageUrl)
        whenever(repository.loadBreedImage(breedId)).thenReturn(breedWithImage)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(breedWithImage, result)
        assertEquals(imageUrl, result?.imageUrl)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should handle different breed difficulties`() = runTest {
        // Given
        val breedId = "border_collie"
        val advancedBreed = createTestBreed(
            breedId, 
            "Border Collie", 
            "https://example.com/border_collie.jpg",
            DogBreed.Difficulty.ADVANCED
        )
        whenever(repository.loadBreedImage(breedId)).thenReturn(advancedBreed)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(advancedBreed, result)
        assertEquals(DogBreed.Difficulty.ADVANCED, result?.difficulty)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should handle breeds with different sizes`() = runTest {
        // Given
        val breedId = "great_dane"
        val largeBreed = createTestBreed(
            breedId, 
            "Great Dane", 
            "https://example.com/great_dane.jpg",
            DogBreed.Difficulty.BEGINNER,
            DogBreed.Size.EXTRA_LARGE
        )
        whenever(repository.loadBreedImage(breedId)).thenReturn(largeBreed)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(largeBreed, result)
        assertEquals(DogBreed.Size.EXTRA_LARGE, result?.size)
        verify(repository).loadBreedImage(breedId)
    }
    
    @Test
    fun `invoke should preserve all breed properties`() = runTest {
        // Given
        val breedId = "german_shepherd"
        val fullBreed = DogBreed(
            id = breedId,
            name = "German Shepherd",
            imageUrl = "https://example.com/german_shepherd.jpg",
            description = "Large, athletic dogs, they are extremely versatile.",
            funFact = "German Shepherds are the second most popular dog breed!",
            origin = "Germany",
            size = DogBreed.Size.LARGE,
            temperament = listOf("Confident", "Courageous", "Smart"),
            lifeSpan = "9-13 years",
            difficulty = DogBreed.Difficulty.INTERMEDIATE,
            isFavorite = true
        )
        whenever(repository.loadBreedImage(breedId)).thenReturn(fullBreed)
        
        // When
        val result = useCase.invoke(breedId)
        
        // Then
        assertEquals(fullBreed, result)
        assertEquals("Large, athletic dogs, they are extremely versatile.", result?.description)
        assertEquals("German Shepherds are the second most popular dog breed!", result?.funFact)
        assertEquals("Germany", result?.origin)
        assertEquals(listOf("Confident", "Courageous", "Smart"), result?.temperament)
        assertEquals("9-13 years", result?.lifeSpan)
        assertEquals(true, result?.isFavorite)
        verify(repository).loadBreedImage(breedId)
    }
    
    private fun createTestBreed(
        id: String,
        name: String,
        imageUrl: String = "https://example.com/image.jpg",
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        size: DogBreed.Size = DogBreed.Size.MEDIUM
    ): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = "Test description for $name",
            funFact = "Test fun fact for $name",
            origin = "Test origin",
            size = size,
            temperament = listOf("Friendly", "Intelligent"),
            lifeSpan = "10-12 years",
            difficulty = difficulty,
            isFavorite = false
        )
    }
}