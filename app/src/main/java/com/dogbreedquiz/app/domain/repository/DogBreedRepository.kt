package com.dogbreedquiz.app.domain.repository

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizSession
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for dog breed data operations
 * This interface defines the contract for data access without exposing implementation details
 */
interface DogBreedRepository {
    
    /**
     * Get all available dog breeds
     * @param forceRefresh Force refresh from remote source
     * @return List of dog breeds
     */
    suspend fun getAllBreeds(forceRefresh: Boolean = false): List<DogBreed>
    
    /**
     * Get all breeds as Flow for reactive UI updates
     */
    fun getAllBreedsFlow(): Flow<List<DogBreed>>
    
    /**
     * Get breed by ID
     * @param id Unique breed identifier
     * @return DogBreed if found, null otherwise
     */
    suspend fun getBreedById(id: String): DogBreed?
    
    /**
     * Get breed by ID as Flow
     */
    fun getBreedByIdFlow(id: String): Flow<DogBreed?>
    
    /**
     * Get breeds filtered by difficulty level
     * @param difficulty Difficulty level to filter by
     * @return List of breeds matching the difficulty
     */
    suspend fun getBreedsByDifficulty(difficulty: DogBreed.Difficulty): List<DogBreed>
    
    /**
     * Get breeds filtered by size
     * @param size Size category to filter by
     * @return List of breeds matching the size
     */
    suspend fun getBreedsBySize(size: DogBreed.Size): List<DogBreed>
    
    /**
     * Search breeds by query string
     * @param query Search query
     * @return List of matching breeds
     */
    suspend fun searchBreeds(query: String): List<DogBreed>
    
    /**
     * Get favorite breeds
     * @return List of favorite breeds
     */
    suspend fun getFavoriteBreeds(): List<DogBreed>
    
    /**
     * Get favorite breeds as Flow
     */
    fun getFavoriteBreedsFlow(): Flow<List<DogBreed>>
    
    /**
     * Update breed favorite status
     * @param breedId Breed identifier
     * @param isFavorite New favorite status
     */
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean)
    
    /**
     * Get random breeds
     * @param count Number of random breeds to return
     * @return List of random breeds
     */
    suspend fun getRandomBreeds(count: Int): List<DogBreed>
    
    /**
     * Load image for a specific breed
     * @param breedId Breed identifier
     * @return DogBreed with loaded image URL
     */
    suspend fun loadBreedImage(breedId: String): DogBreed?
    
    /**
     * Load images for multiple breeds efficiently
     * @param breedIds List of breed identifiers
     * @return Map of breed ID to image URL
     */
    suspend fun loadBreedImages(breedIds: List<String>): Map<String, String>
}