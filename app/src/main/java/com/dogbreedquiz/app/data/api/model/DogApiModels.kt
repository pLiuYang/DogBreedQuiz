package com.dogbreedquiz.app.data.api.model

import kotlinx.serialization.Serializable

/**
 * Response model for the breeds list API endpoint
 * GET https://dog.ceo/api/breeds/list/all
 */
@Serializable
data class BreedsListResponse(
    val message: Map<String, List<String>>,
    val status: String
)

/**
 * Response model for random breed image API endpoint
 * GET https://dog.ceo/api/breed/{breed}/{subBreed}/images/random
 * GET https://dog.ceo/api/breed/{breed}/images/random
 */
@Serializable
data class RandomImageResponse(
    val message: String,
    val status: String
)

/**
 * Response model for multiple random images
 * GET https://dog.ceo/api/breed/{breed}/images/random/{count}
 */
@Serializable
data class MultipleImagesResponse(
    val message: List<String>,
    val status: String
)

/**
 * Internal model representing a breed with its sub-breeds
 */
data class ApiBreed(
    val name: String,
    val subBreeds: List<String> = emptyList()
) {
    /**
     * Returns the full breed identifier for API calls
     * For main breeds: "retriever"
     * For sub-breeds: "retriever/golden"
     */
    fun getApiIdentifier(subBreed: String? = null): String {
        return if (subBreed != null && subBreed in subBreeds) {
            "$name/$subBreed"
        } else {
            name
        }
    }
    
    /**
     * Returns display name for the breed
     * For main breeds: "Retriever"
     * For sub-breeds: "Golden Retriever"
     */
    fun getDisplayName(subBreed: String? = null): String {
        return if (subBreed != null && subBreed in subBreeds) {
            "${subBreed.replaceFirstChar { it.uppercase() }} ${name.replaceFirstChar { it.uppercase() }}"
        } else {
            name.replaceFirstChar { it.uppercase() }
        }
    }
    
    /**
     * Returns unique identifier for internal use
     */
    fun getUniqueId(subBreed: String? = null): String {
        return if (subBreed != null && subBreed in subBreeds) {
            "${name}_${subBreed}"
        } else {
            name
        }.lowercase().replace(" ", "_")
    }
}

/**
 * Sealed class representing API call results
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val exception: Throwable) : ApiResult<Nothing>()
    data class Loading(val message: String = "Loading...") : ApiResult<Nothing>()
}

/**
 * Exception classes for API errors
 */
sealed class DogApiException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkException(message: String, cause: Throwable? = null) : DogApiException(message, cause)
    class ApiException(message: String, cause: Throwable? = null) : DogApiException(message, cause)
    class ParseException(message: String, cause: Throwable? = null) : DogApiException(message, cause)
    class UnknownException(message: String, cause: Throwable? = null) : DogApiException(message, cause)
}