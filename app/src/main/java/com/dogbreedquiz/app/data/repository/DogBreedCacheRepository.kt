package com.dogbreedquiz.app.data.repository

import com.dogbreedquiz.app.data.api.model.ApiResult
import com.dogbreedquiz.app.data.api.repository.DogApiRepository
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import com.dogbreedquiz.app.data.database.entity.ImageCacheEntity
import com.dogbreedquiz.app.data.mapper.DogBreedMapper
import com.dogbreedquiz.app.data.model.DogBreed
import com.dogbreedquiz.app.data.model.QuizQuestion
import com.dogbreedquiz.app.data.model.QuizSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Repository for dog breed data with comprehensive Room database caching
 * Implements cache-first strategy with 7-day expiration and background refresh
 */
@Singleton
class DogBreedCacheRepository @Inject constructor(
    private val dogApiRepository: DogApiRepository,
    private val breedDao: BreedDao,
    private val imageCacheDao: ImageCacheDao,
    private val cacheStatsDao: CacheStatsDao
) {
    
    companion object {
        private const val CACHE_DURATION_DAYS = 7L
        private const val NEAR_EXPIRATION_THRESHOLD_HOURS = 24L
        private const val MAX_CACHE_SIZE_MB = 100L
        private const val MAX_IMAGES_PER_BREED = 5
        private const val QUIZ_GENERATION_RETRIES = 3
    }
    
    // ===== PUBLIC API METHODS =====
    
    /**
     * Get all available dog breeds using cache-first strategy
     * Falls back to API if cache is empty or expired
     * Images are loaded lazily when needed
     */
    suspend fun getAllBreeds(forceRefresh: Boolean = false): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            try {
                // Record cache access attempt
                recordCacheAccess(CacheStatsEntity.CacheType.BREEDS)
                
                // Check cache first (unless force refresh)
                if (!forceRefresh) {
                    val cachedBreeds = breedDao.getValidBreeds()
                    if (cachedBreeds.isNotEmpty()) {
                        // Cache hit - return breeds without images initially
                        recordCacheHit(CacheStatsEntity.CacheType.BREEDS)
                        return@withContext cachedBreeds.map { breedEntity ->
                            breedEntity.toDogBreed("") // Return with empty image URL initially
                        }
                    }
                }
                
                // Cache miss - fetch from API
                recordCacheMiss(CacheStatsEntity.CacheType.BREEDS)
                val apiResult = dogApiRepository.getAllBreeds(forceRefresh)
                
                when (apiResult) {
                    is ApiResult.Success -> {
                        val apiBreeds = apiResult.data
                        val dogBreeds = mutableListOf<DogBreed>()
                        val breedEntities = mutableListOf<BreedEntity>()
                        
                        // Process API breeds (limit to reasonable number)
                        val selectedBreeds = apiBreeds.take(50)
                        
                        for (apiBreed in selectedBreeds) {
                            try {
                                if (apiBreed.subBreeds.isEmpty()) {
                                    // Main breed without sub-breeds - create without image initially
                                    val dogBreed = DogBreedMapper.mapApiBreedToDogBreed(
                                        apiBreed = apiBreed,
                                        subBreed = null,
                                        imageUrl = "" // Empty image URL initially
                                    )
                                    dogBreeds.add(dogBreed)
                                    
                                    // Create database entities
                                    val breedEntity = BreedEntity.fromDogBreed(dogBreed)
                                    breedEntities.add(breedEntity)
                                } else {
                                    // Add sub-breeds (limit to 3 per main breed)
                                    val subBreedsToAdd = apiBreed.subBreeds.take(3)
                                    for (subBreed in subBreedsToAdd) {
                                        val dogBreed = DogBreedMapper.mapApiBreedToDogBreed(
                                            apiBreed = apiBreed,
                                            subBreed = subBreed,
                                            imageUrl = "" // Empty image URL initially
                                        )
                                        dogBreeds.add(dogBreed)
                                        
                                        // Create database entities
                                        val breedEntity = BreedEntity.fromDogBreed(dogBreed)
                                        breedEntities.add(breedEntity)
                                    }
                                }
                            } catch (e: Exception) {
                                // Skip this breed if there's an error
                                continue
                            }
                        }
                        
                        // Cache the breed data in database (without images initially)
                        if (breedEntities.isNotEmpty()) {
                            breedDao.insertBreeds(breedEntities)
                            
                            // Record cache statistics
                            recordCachedItems(
                                CacheStatsEntity.CacheType.BREEDS,
                                breedEntities.size
                            )
                        }
                        
                        dogBreeds
                    }
                    is ApiResult.Error -> {
                        // API failed - try to return cached data even if expired
                        val cachedBreeds = breedDao.getAllBreeds()
                        if (cachedBreeds.isNotEmpty()) {
                            cachedBreeds.map { breedEntity ->
                                breedEntity.toDogBreed("") // Return with empty image URL
                            }
                        } else {
                            // Return fallback static data
                            getFallbackBreeds().map { it.copy(imageUrl = "") }
                        }
                    }
                    is ApiResult.Loading -> {
                        // This shouldn't happen in this context
                        val cachedBreeds = breedDao.getValidBreeds()
                        cachedBreeds.map { breedEntity ->
                            breedEntity.toDogBreed("") // Return with empty image URL
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle any unexpected errors
                val cachedBreeds = breedDao.getAllBreeds()
                if (cachedBreeds.isNotEmpty()) {
                    cachedBreeds.map { breedEntity ->
                        breedEntity.toDogBreed("") // Return with empty image URL
                    }
                } else {
                    getFallbackBreeds().map { it.copy(imageUrl = "") }
                }
            }
        }
    }
    
    /**
     * Get all breeds as Flow for reactive UI updates
     * Images are loaded lazily when needed
     */
    fun getAllBreedsFlow(): Flow<List<DogBreed>> {
        return breedDao.getValidBreedsFlow().map { breedEntities ->
            breedEntities.map { breedEntity ->
                breedEntity.toDogBreed("") // Return with empty image URL initially
            }
        }
    }
    
    /**
     * Get breed by ID from cache or API
     */
    suspend fun getBreedById(id: String): DogBreed? {
        return withContext(Dispatchers.IO) {
            val breedEntity = breedDao.getBreedById(id)
            if (breedEntity != null && breedEntity.isValid()) {
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            } else {
                // Try to fetch from API if not in cache
                val allBreeds = getAllBreeds()
                allBreeds.find { it.id == id }
            }
        }
    }

    /**
     * Load image for a specific breed lazily
     * Returns the breed with image URL if successful
     */
    suspend fun loadBreedImage(breedId: String): DogBreed? {
        return withContext(Dispatchers.IO) {
            try {
                val breedEntity = breedDao.getBreedById(breedId)
                if (breedEntity == null) {
                    return@withContext null
                }

                // Check if image already exists in cache
                val existingImage = imageCacheDao.getPrimaryImageForBreed(breedId)
                if (existingImage != null && existingImage.isValid()) {
                    return@withContext breedEntity.toDogBreed(existingImage.imageUrl)
                }

                // Parse breed name and sub-breed from the breed ID
                // Breed IDs are in format: "main_breed" or "main_breed-sub_breed"
                val breedParts = breedId.split("-")
                val mainBreed = breedParts[0].replace("_", " ")
                val subBreed = if (breedParts.size > 1) breedParts[1].replace("_", " ") else null
                
                val imageResult = if (subBreed != null) {
                    dogApiRepository.getRandomBreedImage(mainBreed, subBreed)
                } else {
                    dogApiRepository.getRandomBreedImage(mainBreed)
                }

                when (imageResult) {
                    is ApiResult.Success -> {
                        val imageUrl = imageResult.data
                        val imageEntity = ImageCacheEntity.create(
                            breedId = breedId,
                            imageUrl = imageUrl,
                            imageType = ImageCacheEntity.ImageType.PRIMARY,
                            isPrimary = true
                        )
                        
                        imageCacheDao.insertImages(listOf(imageEntity))
                        recordCachedItems(
                            CacheStatsEntity.CacheType.IMAGES,
                            1,
                            imageEntity.fileSize
                        )
                        
                        breedEntity.toDogBreed(imageUrl)
                    }
                    else -> {
                        // Return breed with empty image URL if API fails
                        breedEntity.toDogBreed("")
                    }
                }
            } catch (e: Exception) {
                // Return breed with empty image URL on error
                val breedEntity = breedDao.getBreedById(breedId)
                breedEntity?.toDogBreed("")
            }
        }
    }

    /**
     * Load images for multiple breeds efficiently
     */
    suspend fun loadBreedImages(breedIds: List<String>): Map<String, String> {
        return withContext(Dispatchers.IO) {
            val results = mutableMapOf<String, String>()
            
            for (breedId in breedIds) {
                val breedWithImage = loadBreedImage(breedId)
                results[breedId] = breedWithImage?.imageUrl ?: ""
            }
            
            results
        }
    }
    
    /**
     * Get breed by ID as Flow
     */
    fun getBreedByIdFlow(id: String): Flow<DogBreed?> {
        return breedDao.getBreedByIdFlow(id).map { breedEntity ->
            if (breedEntity != null && breedEntity.isValid()) {
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            } else {
                null
            }
        }
    }
    
    /**
     * Get breeds filtered by difficulty
     */
    suspend fun getBreedsByDifficulty(difficulty: DogBreed.Difficulty): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            val breedEntities = breedDao.getBreedsByDifficulty(difficulty.name)
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    /**
     * Get breeds filtered by size
     */
    suspend fun getBreedsBySize(size: DogBreed.Size): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            val breedEntities = breedDao.getBreedsBySize(size.name)
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    /**
     * Search breeds by query
     */
    suspend fun searchBreeds(query: String): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            val breedEntities = breedDao.searchBreeds(query)
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    /**
     * Get favorite breeds
     */
    suspend fun getFavoriteBreeds(): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            val breedEntities = breedDao.getFavoriteBreeds()
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    /**
     * Get favorite breeds as Flow
     */
    fun getFavoriteBreedsFlow(): Flow<List<DogBreed>> {
        return breedDao.getFavoriteBreedsFlow().map { breedEntities ->
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    /**
     * Update breed favorite status
     */
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean) {
        withContext(Dispatchers.IO) {
            breedDao.updateFavoriteStatus(breedId, isFavorite)
        }
    }
    
    /**
     * Generate quiz session with cached breeds
     * Images are loaded lazily when the quiz is displayed
     */
    suspend fun generateQuizSession(
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        questionCount: Int = 10
    ): QuizSession {
        return withContext(Dispatchers.IO) {
            var attempts = 0
            var availableBreeds: List<DogBreed>
            
            do {
                availableBreeds = getBreedsByDifficulty(difficulty)
                attempts++
                
                // If not enough breeds for difficulty, try mixed difficulty
                if (availableBreeds.size < 4 && attempts < QUIZ_GENERATION_RETRIES) {
                    availableBreeds = getAllBreeds().shuffled()
                }
            } while (availableBreeds.size < 4 && attempts < QUIZ_GENERATION_RETRIES)
            
            if (availableBreeds.size < 4) {
                throw IllegalStateException("Not enough breeds available for quiz generation")
            }
            
            val questions = mutableListOf<QuizQuestion>()
            val usedBreeds = mutableSetOf<String>()
            
            repeat(questionCount.coerceAtMost(availableBreeds.size)) {
                val correctBreed = availableBreeds.filter { it.id !in usedBreeds }.randomOrNull()
                    ?: availableBreeds.random()
                
                usedBreeds.add(correctBreed.id)
                
                val incorrectOptions = availableBreeds
                    .filter { it.id != correctBreed.id }
                    .shuffled()
                    .take(3)
                
                val allOptions = (incorrectOptions + correctBreed).shuffled()
                
                questions.add(
                    QuizQuestion(
                        id = "question_${questions.size + 1}",
                        correctBreed = correctBreed,
                        options = allOptions,
                        imageUrl = "" // Image will be loaded lazily
                    )
                )
            }
            
            QuizSession(
                id = "session_${System.currentTimeMillis()}",
                questions = questions
            )
        }
    }
    
    /**
     * Get random breeds for various purposes
     */
    suspend fun getRandomBreeds(count: Int): List<DogBreed> {
        return withContext(Dispatchers.IO) {
            val breedEntities = breedDao.getRandomBreeds(count)
            breedEntities.map { breedEntity ->
                val primaryImage = imageCacheDao.getPrimaryImageForBreed(breedEntity.id)
                breedEntity.toDogBreed(primaryImage?.imageUrl ?: "")
            }
        }
    }
    
    // ===== CACHE MANAGEMENT METHODS =====
    
    /**
     * Clear all cached data
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            breedDao.deleteAllBreeds()
            imageCacheDao.deleteAllImages()
            recordCacheCleared(CacheStatsEntity.CacheType.COMBINED)
        }
    }
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache() {
        withContext(Dispatchers.IO) {
            val expiredBreeds = breedDao.deleteExpiredBreeds()
            val expiredImages = imageCacheDao.deleteExpiredImages()
            
            if (expiredBreeds > 0 || expiredImages > 0) {
                recordExpiredItems(CacheStatsEntity.CacheType.COMBINED, expiredBreeds + expiredImages)
            }
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStatistics(): CacheStatistics {
        return withContext(Dispatchers.IO) {
            val breedStats = breedDao.getCacheStatistics()
            val imageStats = imageCacheDao.getImageCacheStatistics()
            
            CacheStatistics(
                totalBreeds = breedStats.totalBreeds,
                validBreeds = breedStats.validBreeds,
                expiredBreeds = breedStats.expiredBreeds,
                favoriteBreeds = breedStats.favoriteBreeds,
                totalImages = imageStats.totalImages,
                validImages = imageStats.validImages,
                expiredImages = imageStats.expiredImages,
                totalCacheSize = imageStats.totalSizeBytes,
                cacheHitRate = getCacheHitRate(),
                oldestCacheTime = minOf(breedStats.oldestCacheTime, imageStats.oldestCacheTime),
                newestCacheTime = maxOf(breedStats.newestCacheTime, imageStats.newestCacheTime)
            )
        }
    }
    
    /**
     * Perform background cache refresh for items near expiration
     */
    suspend fun performBackgroundRefresh() {
        withContext(Dispatchers.IO) {
            try {
                val breedsNearExpiration = breedDao.getBreedsNearExpiration()
                val imagesNearExpiration = imageCacheDao.getImagesNearExpiration()
                
                if (breedsNearExpiration.isNotEmpty() || imagesNearExpiration.isNotEmpty()) {
                    // Refresh data from API
                    getAllBreeds(forceRefresh = true)
                }
            } catch (e: Exception) {
                // Log error but don't throw - background refresh should be silent
                android.util.Log.w("DogBreedCacheRepository", "Background refresh failed", e)
            }
        }
    }
    
    /**
     * Optimize cache by removing least accessed items if over size limit
     */
    suspend fun optimizeCache() {
        withContext(Dispatchers.IO) {
            try {
                val currentCacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                val maxCacheSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
                
                if (currentCacheSize > maxCacheSizeBytes) {
                    val excessSize = currentCacheSize - maxCacheSizeBytes
                    val itemsToDelete = (excessSize / (1024 * 1024)).toInt() + 5 // Rough estimate
                    
                    imageCacheDao.deleteLeastAccessedImages(itemsToDelete)
                }
                
                // Clean up expired entries
                clearExpiredCache()
                
            } catch (e: Exception) {
                android.util.Log.w("DogBreedCacheRepository", "Cache optimization failed", e)
            }
        }
    }
    
    // ===== PRIVATE HELPER METHODS =====
    
    /**
     * Get fallback breeds when API and cache both fail
     */
    private fun getFallbackBreeds(): List<DogBreed> {
        return listOf(
            DogBreed(
                id = "golden_retriever",
                name = "Golden Retriever",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop",
                description = "A friendly, intelligent, and devoted dog. Golden Retrievers are serious workers at hunting and field work, as guides for the blind, and in search-and-rescue.",
                funFact = "Golden Retrievers were originally bred in Scotland for hunting waterfowl!",
                origin = "Scotland",
                size = DogBreed.Size.LARGE,
                temperament = listOf("Friendly", "Intelligent", "Devoted"),
                lifeSpan = "10-12 years",
                difficulty = DogBreed.Difficulty.BEGINNER
            ),
            DogBreed(
                id = "labrador_retriever",
                name = "Labrador Retriever",
                imageUrl = "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&h=300&fit=crop",
                description = "Labs are friendly, outgoing, and active companions who have more than enough affection to go around for a family looking for a medium to large dog.",
                funFact = "Labradors are the most popular dog breed in the United States!",
                origin = "Newfoundland, Canada",
                size = DogBreed.Size.LARGE,
                temperament = listOf("Outgoing", "Even Tempered", "Gentle"),
                lifeSpan = "10-12 years",
                difficulty = DogBreed.Difficulty.BEGINNER
            )
        )
    }
    
    /**
     * Record cache access attempt
     */
    private suspend fun recordCacheAccess(cacheType: CacheStatsEntity.CacheType) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            // This will be recorded as either hit or miss in subsequent calls
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Record cache hit
     */
    private suspend fun recordCacheHit(cacheType: CacheStatsEntity.CacheType) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            cacheStatsDao.recordCacheHit(today, cacheType)
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Record cache miss
     */
    private suspend fun recordCacheMiss(cacheType: CacheStatsEntity.CacheType) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            cacheStatsDao.recordCacheMiss(today, cacheType)
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Record cached items
     */
    private suspend fun recordCachedItems(
        cacheType: CacheStatsEntity.CacheType,
        count: Int,
        bytes: Long = 0L
    ) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            repeat(count) {
                cacheStatsDao.recordCachedItem(today, cacheType, bytes / count)
            }
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Record expired items
     */
    private suspend fun recordExpiredItems(cacheType: CacheStatsEntity.CacheType, count: Int) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            cacheStatsDao.recordExpiredItems(today, cacheType, count)
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Record cache cleared
     */
    private suspend fun recordCacheCleared(cacheType: CacheStatsEntity.CacheType) {
        try {
            val today = CacheStatsEntity.getTodayDateString()
            cacheStatsDao.recordCacheCleared(today, cacheType)
        } catch (e: Exception) {
            // Ignore statistics errors
        }
    }
    
    /**
     * Get cache hit rate from statistics
     */
    private suspend fun getCacheHitRate(): Float {
        return try {
            val today = CacheStatsEntity.getTodayDateString()
            val stats = cacheStatsDao.getStatsForDate(today, CacheStatsEntity.CacheType.COMBINED)
            stats?.getCacheHitRate() ?: 0f
        } catch (e: Exception) {
            0f
        }
    }
}

/**
 * Data class for comprehensive cache statistics
 */
data class CacheStatistics(
    val totalBreeds: Int,
    val validBreeds: Int,
    val expiredBreeds: Int,
    val favoriteBreeds: Int,
    val totalImages: Int,
    val validImages: Int,
    val expiredImages: Int,
    val totalCacheSize: Long,
    val cacheHitRate: Float,
    val oldestCacheTime: Long,
    val newestCacheTime: Long
)