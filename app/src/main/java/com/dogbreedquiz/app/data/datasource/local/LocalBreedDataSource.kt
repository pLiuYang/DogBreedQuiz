package com.dogbreedquiz.app.data.datasource.local

import com.dogbreedquiz.app.data.database.entity.BreedEntity
import kotlinx.coroutines.flow.Flow

/**
 * Interface for local breed data operations
 * Abstracts the local storage implementation (Room database)
 */
interface LocalBreedDataSource {
    
    /**
     * Get all breeds from local storage
     */
    suspend fun getAllBreeds(): List<BreedEntity>
    
    /**
     * Get all breeds as Flow for reactive updates
     */
    fun getAllBreedsFlow(): Flow<List<BreedEntity>>
    
    /**
     * Get breed by ID from local storage
     */
    suspend fun getBreedById(id: String): BreedEntity?
    
    /**
     * Get breed by ID as Flow
     */
    fun getBreedByIdFlow(id: String): Flow<BreedEntity?>
    
    /**
     * Save breeds to local storage
     */
    suspend fun saveBreeds(breeds: List<BreedEntity>)
    
    /**
     * Save single breed to local storage
     */
    suspend fun saveBreed(breed: BreedEntity)
    
    /**
     * Update breed image URL
     */
    suspend fun updateBreedImage(breedId: String, imageUrl: String)
    
    /**
     * Get breeds by difficulty level
     */
    suspend fun getBreedsByDifficulty(difficulty: String): List<BreedEntity>
    
    /**
     * Get breeds by size
     */
    suspend fun getBreedsBySize(size: String): List<BreedEntity>
    
    /**
     * Search breeds by query
     */
    suspend fun searchBreeds(query: String): List<BreedEntity>
    
    /**
     * Get favorite breeds
     */
    suspend fun getFavoriteBreeds(): List<BreedEntity>
    
    /**
     * Get favorite breeds as Flow
     */
    fun getFavoriteBreedsFlow(): Flow<List<BreedEntity>>
    
    /**
     * Update favorite status
     */
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean)
    
    /**
     * Get random breeds
     */
    suspend fun getRandomBreeds(count: Int): List<BreedEntity>
    
    /**
     * Clear all breeds from local storage
     */
    suspend fun clearAllBreeds()
    
    /**
     * Check if local data is valid (not expired)
     */
    suspend fun isDataValid(): Boolean
}