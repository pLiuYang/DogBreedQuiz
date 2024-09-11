package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving all dog breeds
 * Encapsulates the business logic for fetching breeds
 */
class GetAllBreedsUseCase @Inject constructor(
    private val repository: DogBreedRepository
) {
    
    /**
     * Execute the use case to get all breeds
     * @param forceRefresh Whether to force refresh from remote source
     * @return List of dog breeds
     */
    suspend operator fun invoke(forceRefresh: Boolean = false): List<DogBreed> {
        return repository.getAllBreeds(forceRefresh)
    }
    
    /**
     * Get all breeds as Flow for reactive updates
     */
    fun asFlow(): Flow<List<DogBreed>> {
        return repository.getAllBreedsFlow()
    }
}