package com.dogbreedquiz.app.data.datasource.remote

import com.dogbreedquiz.app.data.database.entity.BreedEntity

/**
 * Interface for remote breed data operations
 * Abstracts the API implementation details
 */
interface RemoteBreedDataSource {
    
    /**
     * Fetch all breeds from remote API
     * @return List of breed entities from API
     */
    suspend fun getAllBreeds(): List<BreedEntity>
    
    /**
     * Get image URL for a specific breed
     * @param breedId Breed identifier
     * @return Image URL string
     */
    suspend fun getBreedImage(breedId: String): String
    
    /**
     * Get multiple images for a breed
     * @param breedId Breed identifier
     * @param count Number of images to fetch
     * @return List of image URLs
     */
    suspend fun getBreedImages(breedId: String, count: Int = 4): List<String>
    
    /**
     * Check if remote service is available
     * @return True if service is reachable, false otherwise
     */
    suspend fun isServiceAvailable(): Boolean
}