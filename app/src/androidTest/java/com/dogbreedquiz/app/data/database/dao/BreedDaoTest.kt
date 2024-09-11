package com.dogbreedquiz.app.data.database.dao

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dogbreedquiz.app.data.database.DogBreedDatabase
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Android instrumentation tests for BreedDao
 * Tests database operations with real Room database on Android device/emulator
 */
@RunWith(AndroidJUnit4::class)
class BreedDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: DogBreedDatabase
    private lateinit var breedDao: BreedDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DogBreedDatabase::class.java
        ).allowMainThreadQueries().build()

        breedDao = database.breedDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ===== INSERT TESTS =====

    @Test
    fun insertBreed_shouldInsertSuccessfully() = runTest {
        // Given
        val breed = createTestBreedEntity("golden_retriever", "Golden Retriever")

        // When
        breedDao.insertBreed(breed)

        // Then
        val retrievedBreed = breedDao.getBreedById("golden_retriever")
        assertNotNull(retrievedBreed)
        assertEquals("Golden Retriever", retrievedBreed.name)
    }

    @Test
    fun insertBreeds_shouldInsertMultipleBreedsSuccessfully() = runTest {
        // Given
        val breeds = listOf(
            createTestBreedEntity("golden_retriever", "Golden Retriever"),
            createTestBreedEntity("labrador_retriever", "Labrador Retriever"),
            createTestBreedEntity("german_shepherd", "German Shepherd")
        )

        // When
        breedDao.insertBreeds(breeds)

        // Then
        val allBreeds = breedDao.getAllBreeds()
        assertEquals(3, allBreeds.size)
        assertTrue(allBreeds.any { it.name == "Golden Retriever" })
        assertTrue(allBreeds.any { it.name == "Labrador Retriever" })
        assertTrue(allBreeds.any { it.name == "German Shepherd" })
    }

    @Test
    fun insertBreed_shouldReplaceOnConflict() = runTest {
        // Given
        val originalBreed = createTestBreedEntity("test_breed", "Original Name")
        val updatedBreed = createTestBreedEntity("test_breed", "Updated Name")

        // When
        breedDao.insertBreed(originalBreed)
        breedDao.insertBreed(updatedBreed)

        // Then
        val retrievedBreed = breedDao.getBreedById("test_breed")
        assertNotNull(retrievedBreed)
        assertEquals("Updated Name", retrievedBreed.name)
    }

    // ===== QUERY TESTS =====

    @Test
    fun getAllBreeds_shouldReturnAllInsertedBreeds() = runTest {
        // Given
        val breeds = listOf(
            createTestBreedEntity("breed1", "Breed 1"),
            createTestBreedEntity("breed2", "Breed 2"),
            createTestBreedEntity("breed3", "Breed 3")
        )
        breedDao.insertBreeds(breeds)

        // When
        val result = breedDao.getAllBreeds()

        // Then
        assertEquals(3, result.size)
        assertEquals(breeds.sortedBy { it.id }, result.sortedBy { it.id })
    }

    @Test
    fun getValidBreeds_shouldReturnOnlyNonExpiredBreeds() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val validBreed = createTestBreedEntity("valid", "Valid Breed").copy(
            expiresAt = currentTime + 86400000L // 24 hours from now
        )
        val expiredBreed = createTestBreedEntity("expired", "Expired Breed").copy(
            expiresAt = currentTime - 86400000L // 24 hours ago
        )

        breedDao.insertBreeds(listOf(validBreed, expiredBreed))

        // When
        val result = breedDao.getValidBreeds()

        // Then
        assertEquals(1, result.size)
        assertEquals("Valid Breed", result[0].name)
    }

    @Test
    fun getBreedById_shouldReturnCorrectBreed() = runTest {
        // Given
        val breed = createTestBreedEntity("target_breed", "Target Breed")
        breedDao.insertBreed(breed)

        // When
        val result = breedDao.getBreedById("target_breed")

        // Then
        assertNotNull(result)
        assertEquals("Target Breed", result.name)
        assertEquals("target_breed", result.id)
    }

    @Test
    fun getBreedById_shouldReturnNullForNonExistentBreed() = runTest {
        // When
        val result = breedDao.getBreedById("nonexistent")

        // Then
        assertNull(result)
    }

    @Test
    fun getBreedsByDifficulty_shouldReturnBreedsWithSpecificDifficulty() = runTest {
        // Given
        val beginnerBreeds = listOf(
            createTestBreedEntity("beginner1", "Beginner 1", "BEGINNER"),
            createTestBreedEntity("beginner2", "Beginner 2", "BEGINNER")
        )
        val intermediateBreed = createTestBreedEntity("intermediate1", "Intermediate 1", "INTERMEDIATE")

        breedDao.insertBreeds(beginnerBreeds + intermediateBreed)

        // When
        val result = breedDao.getBreedsByDifficulty("BEGINNER")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.difficulty == "BEGINNER" })
    }

    @Test
    fun getBreedsBySize_shouldReturnBreedsWithSpecificSize() = runTest {
        // Given
        val largeBreeds = listOf(
            createTestBreedEntity("large1", "Large 1", size = "LARGE"),
            createTestBreedEntity("large2", "Large 2", size = "LARGE")
        )
        val mediumBreed = createTestBreedEntity("medium1", "Medium 1", size = "MEDIUM")

        breedDao.insertBreeds(largeBreeds + mediumBreed)

        // When
        val result = breedDao.getBreedsBySize("LARGE")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.size == "LARGE" })
    }

    @Test
    fun searchBreeds_shouldReturnMatchingBreeds() = runTest {
        // Given
        val breeds = listOf(
            createTestBreedEntity("golden_retriever", "Golden Retriever"),
            createTestBreedEntity("labrador_retriever", "Labrador Retriever"),
            createTestBreedEntity("german_shepherd", "German Shepherd")
        )
        breedDao.insertBreeds(breeds)

        // When
        val result = breedDao.searchBreeds("retriever")

        // Then
        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Golden Retriever" })
        assertTrue(result.any { it.name == "Labrador Retriever" })
    }

    @Test
    fun getFavoriteBreeds_shouldReturnOnlyFavoriteBreeds() = runTest {
        // Given
        val breeds = listOf(
            createTestBreedEntity("favorite1", "Favorite 1", isFavorite = true),
            createTestBreedEntity("favorite2", "Favorite 2", isFavorite = true),
            createTestBreedEntity("notfavorite", "Not Favorite", isFavorite = false)
        )
        breedDao.insertBreeds(breeds)

        // When
        val result = breedDao.getFavoriteBreeds()

        // Then
        assertEquals(2, result.size)
        assertTrue(result.all { it.isFavorite })
    }

    @Test
    fun getFavoriteBreedsFlow_shouldEmitOnlyFavorites() = runTest {
        // Given
        val favoriteBreed = createTestBreedEntity("favorite", "Favorite Breed", isFavorite = true)
        val normalBreed = createTestBreedEntity("normal", "Normal Breed", isFavorite = false)

        breedDao.insertBreeds(listOf(favoriteBreed, normalBreed))

        // When
        val result = breedDao.getFavoriteBreedsFlow().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Favorite Breed", result[0].name)
        assertTrue(result[0].isFavorite)
    }

    // ===== UPDATE TESTS =====

    @Test
    fun updateFavoriteStatus_shouldUpdateCorrectly() = runTest {
        // Given
        val breed = createTestBreedEntity("test_breed", "Test Breed", isFavorite = false)
        breedDao.insertBreed(breed)

        // When
        breedDao.updateFavoriteStatus("test_breed", true)

        // Then
        val updatedBreed = breedDao.getBreedById("test_breed")
        assertNotNull(updatedBreed)
        assertTrue(updatedBreed.isFavorite)
    }

    @Test
    fun updateBreedImage_shouldUpdateImageUrl() = runTest {
        // Given
        val breed = createTestBreedEntity("test_breed", "Test Breed", imageUrl = "")
        breedDao.insertBreed(breed)

        // When
        val newImageUrl = "https://example.com/new_image.jpg"
        breedDao.updateBreedImage("test_breed", newImageUrl)

        // Then
        val updatedBreed = breedDao.getBreedById("test_breed")
        assertNotNull(updatedBreed)
        assertEquals(newImageUrl, updatedBreed.imageUrl)
    }

    // ===== DELETE TESTS =====

    @Test
    fun deleteBreed_shouldRemoveBreedFromDatabase() = runTest {
        // Given
        val breed = createTestBreedEntity("to_delete", "To Delete")
        breedDao.insertBreed(breed)

        // When
        breedDao.deleteBreed(breed)

        // Then
        val deletedBreed = breedDao.getBreedById("to_delete")
        assertNull(deletedBreed)
    }

    @Test
    fun deleteExpiredBreeds_shouldRemoveOnlyExpiredBreeds() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val validBreed = createTestBreedEntity("valid", "Valid Breed").copy(
            expiresAt = currentTime + 86400000L // 24 hours from now
        )
        val expiredBreed = createTestBreedEntity("expired", "Expired Breed").copy(
            expiresAt = currentTime - 86400000L // 24 hours ago
        )

        breedDao.insertBreeds(listOf(validBreed, expiredBreed))

        // When
        val deletedCount = breedDao.deleteExpiredBreeds()

        // Then
        assertEquals(1, deletedCount)
        val remainingBreeds = breedDao.getAllBreeds()
        assertEquals(1, remainingBreeds.size)
        assertEquals("Valid Breed", remainingBreeds[0].name)
    }

    @Test
    fun clearAllBreeds_shouldRemoveAllBreeds() = runTest {
        // Given
        val breeds = listOf(
            createTestBreedEntity("breed1", "Breed 1"),
            createTestBreedEntity("breed2", "Breed 2"),
            createTestBreedEntity("breed3", "Breed 3")
        )
        breedDao.insertBreeds(breeds)

        // When
        breedDao.deleteAllBreeds()

        // Then
        val remainingBreeds = breedDao.getAllBreeds()
        assertTrue(remainingBreeds.isEmpty())
    }

    // ===== CACHE STATISTICS TESTS =====

    @Test
    fun getBreedsNearExpiration_shouldReturnBreedsExpiringWithin24Hours() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val nearExpirationBreed = createTestBreedEntity("near_expiry", "Near Expiry").copy(
            expiresAt = currentTime + 12 * 60 * 60 * 1000L // 12 hours from now
        )
        val farExpirationBreed = createTestBreedEntity("far_expiry", "Far Expiry").copy(
            expiresAt = currentTime + 48 * 60 * 60 * 1000L // 48 hours from now
        )

        breedDao.insertBreeds(listOf(nearExpirationBreed, farExpirationBreed))

        // When
        val result = breedDao.getBreedsNearExpiration()

        // Then
        assertEquals(1, result.size)
        assertEquals("Near Expiry", result[0].name)
    }

    @Test
    fun getCacheStatistics_shouldReturnCorrectCounts() = runTest {
        // Given
        val currentTime = System.currentTimeMillis()
        val validBreeds = listOf(
            createTestBreedEntity("valid1", "Valid 1").copy(expiresAt = currentTime + 86400000L),
            createTestBreedEntity("valid2", "Valid 2").copy(expiresAt = currentTime + 86400000L)
        )
        val expiredBreed = createTestBreedEntity("expired", "Expired").copy(
            expiresAt = currentTime - 86400000L
        )

        breedDao.insertBreeds(validBreeds + expiredBreed)

        // When
        val stats = breedDao.getCacheStatistics()

        // Then
        assertEquals(3, stats.totalBreeds)
        assertEquals(2, stats.validBreeds)
        assertEquals(1, stats.expiredBreeds)
    }

    // ===== HELPER METHODS =====

    private fun createTestBreedEntity(
        id: String,
        name: String,
        difficulty: String = "BEGINNER",
        size: String = "MEDIUM",
        imageUrl: String = "https://example.com/image.jpg",
        isFavorite: Boolean = false
    ): BreedEntity {
        val currentTime = System.currentTimeMillis()
        return BreedEntity(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = "Test description for $name",
            funFact = "Test fun fact for $name",
            origin = "Test origin",
            size = size,
            temperament = "Friendly,Intelligent",
            lifeSpan = "10-12 years",
            difficulty = difficulty,
            isFavorite = isFavorite,
            cachedAt = currentTime,
            expiresAt = currentTime + (7 * 24 * 60 * 60 * 1000L), // 7 days from now
            lastUpdated = currentTime,
            apiSource = "dog.ceo"
        )
    }
}