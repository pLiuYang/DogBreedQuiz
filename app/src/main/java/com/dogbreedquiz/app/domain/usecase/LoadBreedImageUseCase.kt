package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import javax.inject.Inject

/**
 * Use case for loading breed images
 * Handles the business logic for fetching and caching breed images
 */
class LoadBreedImageUseCase @Inject constructor(
    private val breedRepository: DogBreedRepository
) {
    
    /**
     * Load image for a specific breed
     * @param breedId The ID of the breed to load image for
     * @return DogBreed with updated image URL, or null if not found
     */
    suspend operator fun invoke(breedId: String): DogBreed? {
        return try {
            breedRepository.loadBreedImage(breedId)
        } catch (e: Exception) {
            // Log error or handle as needed
            null
        }
    }
}