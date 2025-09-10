package com.dogbreedquiz.app.data.api

import com.dogbreedquiz.app.data.api.model.BreedsListResponse
import com.dogbreedquiz.app.data.api.model.MultipleImagesResponse
import com.dogbreedquiz.app.data.api.model.RandomImageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit service interface for dog.ceo API
 * Base URL: https://dog.ceo/api/
 */
interface DogApiService {
    
    /**
     * Get all available dog breeds and their sub-breeds
     * GET https://dog.ceo/api/breeds/list/all
     */
    @GET("breeds/list/all")
    suspend fun getAllBreeds(): Response<BreedsListResponse>
    
    /**
     * Get random image for a specific breed
     * GET https://dog.ceo/api/breed/{breed}/images/random
     * 
     * @param breed The breed name (e.g., "retriever", "hound")
     */
    @GET("breed/{breed}/images/random")
    suspend fun getRandomBreedImage(@Path("breed") breed: String): Response<RandomImageResponse>
    
    /**
     * Get random image for a specific sub-breed
     * GET https://dog.ceo/api/breed/{breed}/{subBreed}/images/random
     * 
     * @param breed The main breed name (e.g., "retriever")
     * @param subBreed The sub-breed name (e.g., "golden")
     */
    @GET("breed/{breed}/{subBreed}/images/random")
    suspend fun getRandomSubBreedImage(
        @Path("breed") breed: String,
        @Path("subBreed") subBreed: String
    ): Response<RandomImageResponse>
    
    /**
     * Get multiple random images for a breed
     * GET https://dog.ceo/api/breed/{breed}/images/random/{count}
     * 
     * @param breed The breed name
     * @param count Number of images to fetch (max 50)
     */
    @GET("breed/{breed}/images/random/{count}")
    suspend fun getMultipleBreedImages(
        @Path("breed") breed: String,
        @Path("count") count: Int
    ): Response<MultipleImagesResponse>
    
    /**
     * Get multiple random images for a sub-breed
     * GET https://dog.ceo/api/breed/{breed}/{subBreed}/images/random/{count}
     * 
     * @param breed The main breed name
     * @param subBreed The sub-breed name
     * @param count Number of images to fetch (max 50)
     */
    @GET("breed/{breed}/{subBreed}/images/random/{count}")
    suspend fun getMultipleSubBreedImages(
        @Path("breed") breed: String,
        @Path("subBreed") subBreed: String,
        @Path("count") count: Int
    ): Response<MultipleImagesResponse>
}