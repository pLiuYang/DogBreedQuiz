package com.dogbreedquiz.app.data.datasource.remote

import com.dogbreedquiz.app.data.api.model.ApiResult
import com.dogbreedquiz.app.data.api.repository.DogApiClient
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.data.mapper.DogBreedMapper
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of RemoteBreedDataSource using dog.ceo API
 * Handles all remote data operations and API communication
 */
@Singleton
class RemoteBreedDataSourceImpl @Inject constructor(
    private val apiRepository: DogApiClient,
    private val mapper: DogBreedMapper
) : RemoteBreedDataSource {
    
    override suspend fun getAllBreeds(): List<BreedEntity> {
        return when (val result = apiRepository.getAllBreeds()) {
            is ApiResult.Success -> {
                val breedEntities = mutableListOf<BreedEntity>()
                
                // Process API breeds (limit to reasonable number for performance)
                val selectedBreeds = result.data.take(50)
                
                for (apiBreed in selectedBreeds) {
                    try {
                        if (apiBreed.subBreeds.isEmpty()) {
                            // Main breed without sub-breeds
                            val dogBreed = mapper.mapApiBreedToDogBreed(
                                apiBreed = apiBreed,
                                subBreed = null,
                                imageUrl = "" // Images loaded lazily
                            )
                            breedEntities.add(BreedEntity.fromDogBreed(dogBreed))
                        } else {
                            // Add sub-breeds (limit to 3 per main breed)
                            val subBreedsToAdd = apiBreed.subBreeds.take(3)
                            for (subBreed in subBreedsToAdd) {
                                val dogBreed = mapper.mapApiBreedToDogBreed(
                                    apiBreed = apiBreed,
                                    subBreed = subBreed,
                                    imageUrl = "" // Images loaded lazily
                                )
                                breedEntities.add(BreedEntity.fromDogBreed(dogBreed))
                            }
                        }
                    } catch (e: Exception) {
                        // Skip this breed if there's an error
                        continue
                    }
                }
                
                breedEntities
            }
            is ApiResult.Error -> {
                throw Exception("Failed to fetch breeds from API: ${result.exception.message}")
            }
            is ApiResult.Loading -> {
                emptyList() // This shouldn't happen in this context
            }
        }
    }
    
    override suspend fun getBreedImage(breedId: String): String {
        return try {
            // Parse breed name and sub-breed from the breed ID
            val breedParts = breedId.split("-")
            val mainBreed = breedParts[0].replace("_", " ")
            val subBreed = if (breedParts.size > 1) breedParts[1].replace("_", " ") else null
            
            val imageResult = if (subBreed != null) {
                apiRepository.getRandomBreedImage(mainBreed, subBreed)
            } else {
                apiRepository.getRandomBreedImage(mainBreed)
            }
            
            when (imageResult) {
                is ApiResult.Success -> imageResult.data
                is ApiResult.Error -> {
                    throw Exception("Failed to fetch image: ${imageResult.exception.message}")
                }
                is ApiResult.Loading -> ""
            }
        } catch (e: Exception) {
            // Return empty string if image loading fails
            ""
        }
    }
    
    override suspend fun getBreedImages(breedId: String, count: Int): List<String> {
        return try {
            // Parse breed name and sub-breed from the breed ID
            val breedParts = breedId.split("-")
            val mainBreed = breedParts[0].replace("_", " ")
            val subBreed = if (breedParts.size > 1) breedParts[1].replace("_", " ") else null
            
            val imagesResult = if (subBreed != null) {
                apiRepository.getMultipleBreedImages(mainBreed, subBreed, count)
            } else {
                apiRepository.getMultipleBreedImages(mainBreed, count = count)
            }
            
            when (imagesResult) {
                is ApiResult.Success -> imagesResult.data
                is ApiResult.Error -> {
                    // Fallback to single image
                    val singleImage = getBreedImage(breedId)
                    if (singleImage.isNotEmpty()) listOf(singleImage) else emptyList()
                }
                is ApiResult.Loading -> emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    override suspend fun isServiceAvailable(): Boolean {
        return try {
            // Try to fetch a simple breed list to check service availability
            val result = apiRepository.getAllBreeds()
            result is ApiResult.Success
        } catch (e: Exception) {
            false
        }
    }
}