package com.dogbreedquiz.app

import com.dogbreedquiz.app.data.api.DogApiService
import com.dogbreedquiz.app.data.api.repository.DogApiRepository
import com.dogbreedquiz.app.data.mapper.DogBreedMapper
import com.dogbreedquiz.app.data.repository.DogBreedRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Integration test for dog.ceo API
 * This test validates that the API integration works correctly
 */
class ApiIntegrationTest {
    
    private lateinit var dogApiService: DogApiService
    private lateinit var dogApiRepository: DogApiRepository
    private lateinit var dogBreedRepository: DogBreedRepository
    
    @Before
    fun setup() {
        val json = Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            isLenient = true
        }
        
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
        
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://dog.ceo/api/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
        
        dogApiService = retrofit.create(DogApiService::class.java)
        dogApiRepository = DogApiRepository(dogApiService)
        dogBreedRepository = DogBreedRepository(dogApiRepository)
    }
    
    @Test
    fun `test API service can fetch all breeds`() = runBlocking {
        try {
            val response = dogApiService.getAllBreeds()
            assert(response.isSuccessful) { "API call should be successful" }
            
            val body = response.body()
            assert(body != null) { "Response body should not be null" }
            assert(body!!.status == "success") { "API status should be success" }
            assert(body.message.isNotEmpty()) { "Breeds list should not be empty" }
            
            println("✅ Successfully fetched ${body.message.size} breeds from API")
            
            // Print some example breeds
            body.message.take(5).forEach { (breed, subBreeds) ->
                println("  - $breed: ${subBreeds.joinToString(", ")}")
            }
        } catch (e: Exception) {
            println("❌ API test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun `test API service can fetch breed image`() = runBlocking {
        try {
            val response = dogApiService.getRandomBreedImage("retriever")
            assert(response.isSuccessful) { "API call should be successful" }
            
            val body = response.body()
            assert(body != null) { "Response body should not be null" }
            assert(body!!.status == "success") { "API status should be success" }
            assert(body.message.isNotEmpty()) { "Image URL should not be empty" }
            assert(body.message.startsWith("https://")) { "Image URL should be valid HTTPS URL" }
            
            println("✅ Successfully fetched image URL: ${body.message}")
        } catch (e: Exception) {
            println("❌ Image API test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun `test API service can fetch sub-breed image`() = runBlocking {
        try {
            val response = dogApiService.getRandomSubBreedImage("retriever", "golden")
            assert(response.isSuccessful) { "API call should be successful" }
            
            val body = response.body()
            assert(body != null) { "Response body should not be null" }
            assert(body!!.status == "success") { "API status should be success" }
            assert(body.message.isNotEmpty()) { "Image URL should not be empty" }
            assert(body.message.startsWith("https://")) { "Image URL should be valid HTTPS URL" }
            
            println("✅ Successfully fetched sub-breed image URL: ${body.message}")
        } catch (e: Exception) {
            println("❌ Sub-breed image API test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun `test dog breed repository integration`() = runBlocking {
        try {
            val breeds = dogBreedRepository.getAllBreeds()
            assert(breeds.isNotEmpty()) { "Breeds list should not be empty" }
            
            println("✅ Successfully loaded ${breeds.size} breeds through repository")
            
            // Test that breeds have proper data
            val firstBreed = breeds.first()
            assert(firstBreed.id.isNotEmpty()) { "Breed ID should not be empty" }
            assert(firstBreed.name.isNotEmpty()) { "Breed name should not be empty" }
            assert(firstBreed.imageUrl.isNotEmpty()) { "Breed image URL should not be empty" }
            assert(firstBreed.description.isNotEmpty()) { "Breed description should not be empty" }
            
            println("  - First breed: ${firstBreed.name} (${firstBreed.id})")
            println("  - Image URL: ${firstBreed.imageUrl}")
            println("  - Difficulty: ${firstBreed.difficulty}")
            
            // Test quiz session generation
            val quizSession = dogBreedRepository.generateQuizSession()
            assert(quizSession.questions.isNotEmpty()) { "Quiz should have questions" }
            assert(quizSession.questions.size <= 10) { "Quiz should have at most 10 questions" }
            
            val firstQuestion = quizSession.questions.first()
            assert(firstQuestion.options.size == 4) { "Each question should have 4 options" }
            assert(firstQuestion.options.contains(firstQuestion.correctBreed)) { "Correct breed should be in options" }
            
            println("✅ Successfully generated quiz with ${quizSession.questions.size} questions")
            
        } catch (e: Exception) {
            println("❌ Repository integration test failed: ${e.message}")
            throw e
        }
    }
    
    @Test
    fun `test breed mapper functionality`() {
        try {
            // Test mapper with sample data
            val apiBreed = com.dogbreedquiz.app.data.api.model.ApiBreed(
                name = "retriever",
                subBreeds = listOf("golden", "labrador")
            )
            
            val imageUrl = "https://images.dog.ceo/breeds/retriever-golden/n02099601_100.jpg"
            
            // Test main breed mapping
            val mainBreed = DogBreedMapper.mapApiBreedToDogBreed(
                apiBreed = apiBreed,
                subBreed = null,
                imageUrl = imageUrl
            )
            
            assert(mainBreed.name == "Retriever") { "Main breed name should be capitalized" }
            assert(mainBreed.id == "retriever") { "Main breed ID should be lowercase" }
            assert(mainBreed.imageUrl == imageUrl) { "Image URL should match" }
            
            // Test sub-breed mapping
            val subBreed = DogBreedMapper.mapApiBreedToDogBreed(
                apiBreed = apiBreed,
                subBreed = "golden",
                imageUrl = imageUrl
            )
            
            assert(subBreed.name == "Golden Retriever") { "Sub-breed name should include sub-breed" }
            assert(subBreed.id == "retriever_golden") { "Sub-breed ID should include both parts" }
            assert(subBreed.difficulty.ordinal > mainBreed.difficulty.ordinal) { "Sub-breeds should be harder" }
            
            println("✅ Breed mapper working correctly")
            println("  - Main breed: ${mainBreed.name} (${mainBreed.difficulty})")
            println("  - Sub-breed: ${subBreed.name} (${subBreed.difficulty})")
            
        } catch (e: Exception) {
            println("❌ Breed mapper test failed: ${e.message}")
            throw e
        }
    }
}