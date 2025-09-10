package com.dogbreedquiz.app.data.api.repository

import com.dogbreedquiz.app.data.api.DogApiService
import com.dogbreedquiz.app.data.api.model.ApiBreed
import com.dogbreedquiz.app.data.api.model.ApiResult
import com.dogbreedquiz.app.data.api.model.DogApiException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling dog.ceo API calls with caching and error handling
 */
@Singleton
class DogApiRepository @Inject constructor(
    private val apiService: DogApiService
) {
    
    // In-memory cache for breeds (in production, consider using Room database)
    private var cachedBreeds: List<ApiBreed>? = null
    private var breedsLastFetched: Long = 0
    private val cacheValidityDuration = 30 * 60 * 1000L // 30 minutes
    
    // Cache for breed images
    private val imageCache = mutableMapOf<String, String>()
    
    /**
     * Fetch all available dog breeds from the API
     * Uses caching to reduce API calls
     */
    suspend fun getAllBreeds(forceRefresh: Boolean = false): ApiResult<List<ApiBreed>> {
        return withContext(Dispatchers.IO) {
            try {
                // Check cache first
                if (!forceRefresh && isCacheValid() && cachedBreeds != null) {
                    return@withContext ApiResult.Success(cachedBreeds!!)
                }
                
                val response = apiService.getAllBreeds()
                
                if (response.isSuccessful) {
                    val breedsResponse = response.body()
                    if (breedsResponse != null && breedsResponse.status == "success") {
                        val breeds = breedsResponse.message.map { (breedName, subBreeds) ->
                            ApiBreed(
                                name = breedName,
                                subBreeds = subBreeds
                            )
                        }
                        
                        // Update cache
                        cachedBreeds = breeds
                        breedsLastFetched = System.currentTimeMillis()
                        
                        ApiResult.Success(breeds)
                    } else {
                        ApiResult.Error(DogApiException.ApiException("Invalid response format"))
                    }
                } else {
                    ApiResult.Error(
                        DogApiException.ApiException(
                            "API call failed with code: ${response.code()}"
                        )
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(DogApiException.NetworkException("Network error", e))
            } catch (e: Exception) {
                ApiResult.Error(DogApiException.UnknownException("Unknown error", e))
            }
        }
    }
    
    /**
     * Get a random image for a specific breed or sub-breed
     * Uses caching to avoid repeated API calls for the same breed
     */
    suspend fun getRandomBreedImage(
        breed: String,
        subBreed: String? = null
    ): ApiResult<String> {
        return withContext(Dispatchers.IO) {
            try {
                val cacheKey = if (subBreed != null) "${breed}_$subBreed" else breed
                
                // Check cache first (optional - images can be cached for a short time)
                imageCache[cacheKey]?.let { cachedUrl ->
                    // For demo purposes, we'll still fetch fresh images most of the time
                    // but you could implement more sophisticated caching here
                }
                
                val response = if (subBreed != null) {
                    apiService.getRandomSubBreedImage(breed, subBreed)
                } else {
                    apiService.getRandomBreedImage(breed)
                }
                
                if (response.isSuccessful) {
                    val imageResponse = response.body()
                    if (imageResponse != null && imageResponse.status == "success") {
                        val imageUrl = imageResponse.message
                        
                        // Cache the image URL
                        imageCache[cacheKey] = imageUrl
                        
                        ApiResult.Success(imageUrl)
                    } else {
                        ApiResult.Error(DogApiException.ApiException("Invalid image response"))
                    }
                } else {
                    ApiResult.Error(
                        DogApiException.ApiException(
                            "Failed to fetch image with code: ${response.code()}"
                        )
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(DogApiException.NetworkException("Network error", e))
            } catch (e: Exception) {
                ApiResult.Error(DogApiException.UnknownException("Unknown error", e))
            }
        }
    }
    
    /**
     * Get multiple random images for a breed
     */
    suspend fun getMultipleBreedImages(
        breed: String,
        subBreed: String? = null,
        count: Int = 4
    ): ApiResult<List<String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = if (subBreed != null) {
                    apiService.getMultipleSubBreedImages(breed, subBreed, count)
                } else {
                    apiService.getMultipleBreedImages(breed, count)
                }
                
                if (response.isSuccessful) {
                    val imagesResponse = response.body()
                    if (imagesResponse != null && imagesResponse.status == "success") {
                        ApiResult.Success(imagesResponse.message)
                    } else {
                        ApiResult.Error(DogApiException.ApiException("Invalid images response"))
                    }
                } else {
                    ApiResult.Error(
                        DogApiException.ApiException(
                            "Failed to fetch images with code: ${response.code()}"
                        )
                    )
                }
            } catch (e: IOException) {
                ApiResult.Error(DogApiException.NetworkException("Network error", e))
            } catch (e: Exception) {
                ApiResult.Error(DogApiException.UnknownException("Unknown error", e))
            }
        }
    }
    
    /**
     * Clear the breeds cache
     */
    fun clearCache() {
        cachedBreeds = null
        breedsLastFetched = 0
        imageCache.clear()
    }
    
    /**
     * Check if the cached breeds are still valid
     */
    private fun isCacheValid(): Boolean {
        return System.currentTimeMillis() - breedsLastFetched < cacheValidityDuration
    }
    
    /**
     * Get cached breeds without making API call
     */
    fun getCachedBreeds(): List<ApiBreed>? = cachedBreeds
    
    /**
     * Check if breeds are cached and valid
     */
    fun hasCachedBreeds(): Boolean = isCacheValid() && cachedBreeds != null
}

/**
 * Extension function to safely handle Response objects
 */
private inline fun <T> Response<T>.handleResponse(
    onSuccess: (T) -> ApiResult<T>,
    onError: (Int, String?) -> ApiResult<T> = { code, message ->
        ApiResult.Error(DogApiException.ApiException("API error: $code - $message"))
    }
): ApiResult<T> {
    return if (isSuccessful) {
        body()?.let(onSuccess) ?: ApiResult.Error(
            DogApiException.ParseException("Response body is null")
        )
    } else {
        onError(code(), message())
    }
}