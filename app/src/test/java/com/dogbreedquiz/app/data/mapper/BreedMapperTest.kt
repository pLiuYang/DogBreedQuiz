package com.dogbreedquiz.app.data.mapper

import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.domain.model.DogBreed
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for BreedMapperImpl
 * Tests the mapping between database entities and domain models
 */
class BreedMapperTest {

    private lateinit var mapper: BreedMapper

    @Before
    fun setup() {
        mapper = BreedMapperImpl()
    }

    // ===== ENTITY TO DOMAIN MAPPING TESTS =====

    @Test
    fun `mapToDomain should convert BreedEntity to DogBreed correctly`() {
        // Given
        val breedEntity = createTestBreedEntity(
            id = "golden_retriever",
            name = "Golden Retriever",
            description = "A friendly, intelligent, and devoted dog.",
            funFact = "Golden Retrievers were originally bred in Scotland!",
            origin = "Scotland",
            size = "LARGE",
            temperament = listOf("Friendly", "Intelligent", "Devoted"),
            lifeSpan = "10-12 years",
            difficulty = "BEGINNER",
            isFavorite = true,
            imageUrl = "https://example.com/golden_retriever.jpg"
        )

        // When
        val dogBreed = mapper.mapToDomain(breedEntity)

        // Then
        assertEquals("golden_retriever", dogBreed.id)
        assertEquals("Golden Retriever", dogBreed.name)
        assertEquals("A friendly, intelligent, and devoted dog.", dogBreed.description)
        assertEquals("Golden Retrievers were originally bred in Scotland!", dogBreed.funFact)
        assertEquals("Scotland", dogBreed.origin)
        assertEquals(DogBreed.Size.LARGE, dogBreed.size)
        assertEquals(listOf("Friendly", "Intelligent", "Devoted"), dogBreed.temperament)
        assertEquals("10-12 years", dogBreed.lifeSpan)
        assertEquals(DogBreed.Difficulty.BEGINNER, dogBreed.difficulty)
        assertTrue(dogBreed.isFavorite)
        assertEquals("https://example.com/golden_retriever.jpg", dogBreed.imageUrl)
    }

    @Test
    fun `mapToDomain should handle all size enum values correctly`() {
        // Test all size values
        val sizeTests = mapOf(
            "SMALL" to DogBreed.Size.SMALL,
            "MEDIUM" to DogBreed.Size.MEDIUM,
            "LARGE" to DogBreed.Size.LARGE,
            "EXTRA_LARGE" to DogBreed.Size.EXTRA_LARGE
        )

        sizeTests.forEach { (entitySize, expectedDomainSize) ->
            // Given
            val breedEntity = createTestBreedEntity(size = entitySize)

            // When
            val dogBreed = mapper.mapToDomain(breedEntity)

            // Then
            assertEquals(expectedDomainSize, dogBreed.size, "Failed for size: $entitySize")
        }
    }

    @Test
    fun `mapToDomain should handle all difficulty enum values correctly`() {
        // Test all difficulty values
        val difficultyTests = mapOf(
            "BEGINNER" to DogBreed.Difficulty.BEGINNER,
            "INTERMEDIATE" to DogBreed.Difficulty.INTERMEDIATE,
            "ADVANCED" to DogBreed.Difficulty.ADVANCED,
            "EXPERT" to DogBreed.Difficulty.EXPERT
        )

        difficultyTests.forEach { (entityDifficulty, expectedDomainDifficulty) ->
            // Given
            val breedEntity = createTestBreedEntity(difficulty = entityDifficulty)

            // When
            val dogBreed = mapper.mapToDomain(breedEntity)

            // Then
            assertEquals(expectedDomainDifficulty, dogBreed.difficulty, "Failed for difficulty: $entityDifficulty")
        }
    }

    @Test
    fun `mapToDomain should handle single temperament trait`() {
        // Given
        val breedEntity = createTestBreedEntity(temperament = listOf("Friendly"))

        // When
        val dogBreed = mapper.mapToDomain(breedEntity)

        // Then
        assertEquals(listOf("Friendly"), dogBreed.temperament)
    }

    @Test
    fun `mapToDomain should handle multiple temperament traits`() {
        // Given
        val temperamentList = listOf("Friendly", "Intelligent", "Loyal", "Energetic")
        val breedEntity = createTestBreedEntity(temperament = temperamentList)

        // When
        val dogBreed = mapper.mapToDomain(breedEntity)

        // Then
        assertEquals(temperamentList, dogBreed.temperament)
    }

    @Test
    fun `mapToDomain should handle empty strings correctly`() {
        // Given
        val breedEntity = createTestBreedEntity(
            description = "",
            funFact = "",
            origin = "",
            lifeSpan = "",
            imageUrl = ""
        )

        // When
        val dogBreed = mapper.mapToDomain(breedEntity)

        // Then
        assertEquals("", dogBreed.description)
        assertEquals("", dogBreed.funFact)
        assertEquals("", dogBreed.origin)
        assertEquals("", dogBreed.lifeSpan)
        assertEquals("", dogBreed.imageUrl)
    }

    // ===== DOMAIN TO ENTITY MAPPING TESTS =====

    @Test
    fun `mapToEntity should convert DogBreed to BreedEntity correctly`() {
        // Given
        val dogBreed = createTestDogBreed(
            id = "labrador_retriever",
            name = "Labrador Retriever",
            description = "Labs are friendly, outgoing, and active companions.",
            funFact = "Labradors are the most popular dog breed in the United States!",
            origin = "Newfoundland, Canada",
            size = DogBreed.Size.LARGE,
            temperament = listOf("Outgoing", "Even Tempered", "Gentle"),
            lifeSpan = "10-12 years",
            difficulty = DogBreed.Difficulty.BEGINNER,
            isFavorite = false,
            imageUrl = "https://example.com/labrador_retriever.jpg"
        )

        // When
        val breedEntity = mapper.mapToEntity(dogBreed)

        // Then
        assertEquals("labrador_retriever", breedEntity.id)
        assertEquals("Labrador Retriever", breedEntity.name)
        assertEquals("Labs are friendly, outgoing, and active companions.", breedEntity.description)
        assertEquals("Labradors are the most popular dog breed in the United States!", breedEntity.funFact)
        assertEquals("Newfoundland, Canada", breedEntity.origin)
        assertEquals("LARGE", breedEntity.size)
        assertEquals("10-12 years", breedEntity.lifeSpan)
        assertEquals("BEGINNER", breedEntity.difficulty)
        assertFalse(breedEntity.isFavorite)
        assertEquals("https://example.com/labrador_retriever.jpg", breedEntity.imageUrl)
    }

    @Test
    fun `mapToEntity should convert all size enum values correctly`() {
        // Test all size values
        val sizeTests = mapOf(
            DogBreed.Size.SMALL to "SMALL",
            DogBreed.Size.MEDIUM to "MEDIUM",
            DogBreed.Size.LARGE to "LARGE",
            DogBreed.Size.EXTRA_LARGE to "EXTRA_LARGE"
        )

        sizeTests.forEach { (domainSize, expectedEntitySize) ->
            // Given
            val dogBreed = createTestDogBreed(size = domainSize)

            // When
            val breedEntity = mapper.mapToEntity(dogBreed)

            // Then
            assertEquals(expectedEntitySize, breedEntity.size, "Failed for size: $domainSize")
        }
    }

    @Test
    fun `mapToEntity should convert all difficulty enum values correctly`() {
        // Test all difficulty values
        val difficultyTests = mapOf(
            DogBreed.Difficulty.BEGINNER to "BEGINNER",
            DogBreed.Difficulty.INTERMEDIATE to "INTERMEDIATE",
            DogBreed.Difficulty.ADVANCED to "ADVANCED",
            DogBreed.Difficulty.EXPERT to "EXPERT"
        )

        difficultyTests.forEach { (domainDifficulty, expectedEntityDifficulty) ->
            // Given
            val dogBreed = createTestDogBreed(difficulty = domainDifficulty)

            // When
            val breedEntity = mapper.mapToEntity(dogBreed)

            // Then
            assertEquals(expectedEntityDifficulty, breedEntity.difficulty, "Failed for difficulty: $domainDifficulty")
        }
    }

    @Test
    fun `mapToEntity should preserve cache metadata when provided`() {
        // Given
        val dogBreed = createTestDogBreed()
        val currentTime = System.currentTimeMillis()

        // When
        val breedEntity = mapper.mapToEntity(dogBreed)

        // Then
        assertEquals(currentTime, breedEntity.cachedAt)
        assertTrue(breedEntity.expiresAt > currentTime) // Should be set to future time
        assertEquals(currentTime, breedEntity.lastUpdated)
        assertEquals("dog.ceo", breedEntity.apiSource)
    }

    @Test
    fun `mapToEntity should set default cache metadata when not provided`() {
        // Given
        val dogBreed = createTestDogBreed()
        val beforeMapping = System.currentTimeMillis()

        // When
        val breedEntity = mapper.mapToEntity(dogBreed)
        val afterMapping = System.currentTimeMillis()

        // Then
        assertTrue(breedEntity.cachedAt >= beforeMapping)
        assertTrue(breedEntity.cachedAt <= afterMapping)
        assertTrue(breedEntity.expiresAt > breedEntity.cachedAt)
        assertTrue(breedEntity.lastUpdated >= beforeMapping)
        assertTrue(breedEntity.lastUpdated <= afterMapping)
        assertEquals("dog.ceo", breedEntity.apiSource)
    }

    // ===== BIDIRECTIONAL MAPPING TESTS =====

    @Test
    fun `bidirectional mapping should preserve all data`() {
        // Given
        val originalDogBreed = createTestDogBreed(
            id = "german_shepherd",
            name = "German Shepherd",
            size = DogBreed.Size.LARGE,
            difficulty = DogBreed.Difficulty.INTERMEDIATE,
            temperament = listOf("Confident", "Courageous", "Smart"),
            isFavorite = true
        )

        // When - Convert to entity and back to domain
        val breedEntity = mapper.mapToEntity(originalDogBreed)
        val convertedDogBreed = mapper.mapToDomain(breedEntity)

        // Then - All core properties should be preserved
        assertEquals(originalDogBreed.id, convertedDogBreed.id)
        assertEquals(originalDogBreed.name, convertedDogBreed.name)
        assertEquals(originalDogBreed.description, convertedDogBreed.description)
        assertEquals(originalDogBreed.funFact, convertedDogBreed.funFact)
        assertEquals(originalDogBreed.origin, convertedDogBreed.origin)
        assertEquals(originalDogBreed.size, convertedDogBreed.size)
        assertEquals(originalDogBreed.temperament, convertedDogBreed.temperament)
        assertEquals(originalDogBreed.lifeSpan, convertedDogBreed.lifeSpan)
        assertEquals(originalDogBreed.difficulty, convertedDogBreed.difficulty)
        assertEquals(originalDogBreed.isFavorite, convertedDogBreed.isFavorite)
        assertEquals(originalDogBreed.imageUrl, convertedDogBreed.imageUrl)
    }

    @Test
    fun `mapping multiple breeds should work correctly`() {
        // Given
        val dogBreeds = listOf(
            createTestDogBreed(id = "breed1", name = "Breed 1", size = DogBreed.Size.SMALL),
            createTestDogBreed(id = "breed2", name = "Breed 2", size = DogBreed.Size.MEDIUM),
            createTestDogBreed(id = "breed3", name = "Breed 3", size = DogBreed.Size.LARGE)
        )

        // When
        val breedEntities = dogBreeds.map { mapper.mapToEntity(it) }
        val convertedBreeds = breedEntities.map { mapper.mapToDomain(it) }

        // Then
        assertEquals(3, convertedBreeds.size)
        convertedBreeds.forEachIndexed { index, breed ->
            assertEquals(dogBreeds[index].id, breed.id)
            assertEquals(dogBreeds[index].name, breed.name)
            assertEquals(dogBreeds[index].size, breed.size)
        }
    }

    // ===== HELPER METHODS =====

    private fun createTestBreedEntity(
        id: String = "test_breed",
        name: String = "Test Breed",
        description: String = "Test description",
        funFact: String = "Test fun fact",
        origin: String = "Test origin",
        size: String = "MEDIUM",
        temperament: List<String> = listOf("Friendly", "Intelligent"),
        lifeSpan: String = "10-12 years",
        difficulty: String = "BEGINNER",
        isFavorite: Boolean = false,
        imageUrl: String = "https://example.com/image.jpg"
    ): BreedEntity {
        val currentTime = System.currentTimeMillis()
        return BreedEntity(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = description,
            funFact = funFact,
            origin = origin,
            size = size,
            temperament = temperament.joinToString(","),
            lifeSpan = lifeSpan,
            difficulty = difficulty,
            isFavorite = isFavorite,
            cachedAt = currentTime,
            expiresAt = currentTime + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
            lastUpdated = currentTime,
            apiSource = "dog.ceo"
        )
    }

    private fun createTestDogBreed(
        id: String = "test_breed",
        name: String = "Test Breed",
        description: String = "Test description",
        funFact: String = "Test fun fact",
        origin: String = "Test origin",
        size: DogBreed.Size = DogBreed.Size.MEDIUM,
        temperament: List<String> = listOf("Friendly", "Intelligent"),
        lifeSpan: String = "10-12 years",
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        isFavorite: Boolean = false,
        imageUrl: String = "https://example.com/image.jpg"
    ): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = description,
            funFact = funFact,
            origin = origin,
            size = size,
            temperament = temperament,
            lifeSpan = lifeSpan,
            difficulty = difficulty,
            isFavorite = isFavorite
        )
    }
}