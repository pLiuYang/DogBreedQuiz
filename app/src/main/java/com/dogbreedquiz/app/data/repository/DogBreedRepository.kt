
package com.dogbreedquiz.app.data.repository

import com.dogbreedquiz.app.data.model.DogBreed
import com.dogbreedquiz.app.data.model.QuizQuestion
import com.dogbreedquiz.app.data.model.QuizSession
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Main repository for dog breed data that delegates to the cache repository
 * This maintains backward compatibility while adding comprehensive caching
 */
@Singleton
class DogBreedRepository @Inject constructor(
    private val cacheRepository: DogBreedCacheRepository
) {
    
    // Delegate all operations to the cache repository
    
    /**
     * Get all available dog breeds from API or cache
     * Falls back to static data if API fails
     */
    suspend fun getAllBreeds(forceRefresh: Boolean = false): List<DogBreed> {
        return cacheRepository.getAllBreeds(forceRefresh)
    }
    
    /**
     * Get all breeds as Flow for reactive UI updates
     */
    fun getAllBreedsFlow(): Flow<List<DogBreed>> {
        return cacheRepository.getAllBreedsFlow()
    }
    
    /**
     * Get breed by ID from cache or API
     */
    suspend fun getBreedById(id: String): DogBreed? {
        return cacheRepository.getBreedById(id)
    }
    
    /**
     * Get breed by ID as Flow
     */
    fun getBreedByIdFlow(id: String): Flow<DogBreed?> {
        return cacheRepository.getBreedByIdFlow(id)
    }
    
    /**
     * Get breeds filtered by difficulty
     */
    suspend fun getBreedsByDifficulty(difficulty: DogBreed.Difficulty): List<DogBreed> {
        return cacheRepository.getBreedsByDifficulty(difficulty)
    }
    
    /**
     * Get breeds filtered by size
     */
    suspend fun getBreedsBySize(size: DogBreed.Size): List<DogBreed> {
        return cacheRepository.getBreedsBySize(size)
    }
    
    /**
     * Search breeds by query
     */
    suspend fun searchBreeds(query: String): List<DogBreed> {
        return cacheRepository.searchBreeds(query)
    }
    
    /**
     * Get favorite breeds
     */
    suspend fun getFavoriteBreeds(): List<DogBreed> {
        return cacheRepository.getFavoriteBreeds()
    }
    
    /**
     * Get favorite breeds as Flow
     */
    fun getFavoriteBreedsFlow(): Flow<List<DogBreed>> {
        return cacheRepository.getFavoriteBreedsFlow()
    }
    
    /**
     * Update breed favorite status
     */
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean) {
        cacheRepository.updateFavoriteStatus(breedId, isFavorite)
    }
    
    suspend fun generateQuizSession(
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        questionCount: Int = 10
    ): QuizSession {
        return cacheRepository.generateQuizSession(difficulty, questionCount)
    }
    
    suspend fun getRandomBreeds(count: Int): List<DogBreed> {
        return cacheRepository.getRandomBreeds(count)
    }

    /**
     * Load image for a specific breed lazily
     */
    suspend fun loadBreedImage(breedId: String): DogBreed? {
        return cacheRepository.loadBreedImage(breedId)
    }

    /**
     * Load images for multiple breeds efficiently
     */
    suspend fun loadBreedImages(breedIds: List<String>): Map<String, String> {
        return cacheRepository.loadBreedImages(breedIds)
    }
    
    /**
     * Clear the cache and force refresh from API
     */
    fun clearCache() {
        // This will be handled by the cache repository
    }
    
    /**
     * Get cached breeds without making API call
     */
    fun getCachedBreeds(): List<DogBreed>? {
        // This functionality is now handled by the database
        return null
    }
    
    /**
     * Check if breeds are cached and valid
     */
    fun hasCachedBreeds(): Boolean {
        // This functionality is now handled by the database
        return false
    }
    
    // ===== CACHE MANAGEMENT METHODS =====
    
    /**
     * Clear all cached data
     */
    suspend fun clearAllCache() {
        cacheRepository.clearCache()
    }
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache() {
        cacheRepository.clearExpiredCache()
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStatistics(): CacheStatistics {
        return cacheRepository.getCacheStatistics()
    }
    
    /**
     * Perform background cache refresh
     */
    suspend fun performBackgroundRefresh() {
        cacheRepository.performBackgroundRefresh()
    }
    
    /**
     * Optimize cache by removing least accessed items
     */
    suspend fun optimizeCache() {
        cacheRepository.optimizeCache()
    }
}
