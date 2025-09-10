package com.dogbreedquiz.app.data.cache

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import com.dogbreedquiz.app.data.repository.DogBreedCacheRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cache manager for handling background operations, cleanup, and optimization
 */
@Singleton
class CacheManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cacheRepository: DogBreedCacheRepository,
    private val breedDao: BreedDao,
    private val imageCacheDao: ImageCacheDao,
    private val cacheStatsDao: CacheStatsDao
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    companion object {
        private const val CACHE_CLEANUP_WORK_NAME = "cache_cleanup_work"
        private const val CACHE_REFRESH_WORK_NAME = "cache_refresh_work"
        private const val CACHE_OPTIMIZATION_WORK_NAME = "cache_optimization_work"
        
        // Cache management intervals
        private const val CLEANUP_INTERVAL_HOURS = 6L
        private const val REFRESH_INTERVAL_HOURS = 12L
        private const val OPTIMIZATION_INTERVAL_HOURS = 24L
        
        // Cache size limits
        private const val MAX_CACHE_SIZE_MB = 100L
        private const val MAX_IMAGES_PER_BREED = 5
        private const val MAX_TOTAL_BREEDS = 200
    }
    
    /**
     * Initialize cache management with periodic background tasks
     */
    fun initialize() {
        scope.launch {
            try {
                // Schedule periodic cleanup
                schedulePeriodicCleanup()
                
                // Schedule periodic refresh
                schedulePeriodicRefresh()
                
                // Schedule periodic optimization
                schedulePeriodicOptimization()
                
                // Perform initial cleanup
                performInitialCleanup()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheManager", "Failed to initialize cache manager", e)
            }
        }
    }
    
    /**
     * Schedule periodic cache cleanup using WorkManager
     */
    private fun schedulePeriodicCleanup() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val cleanupRequest = PeriodicWorkRequestBuilder<CacheCleanupWorker>(
            CLEANUP_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CACHE_CLEANUP_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
    
    /**
     * Schedule periodic cache refresh using WorkManager
     */
    private fun schedulePeriodicRefresh() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        val refreshRequest = PeriodicWorkRequestBuilder<CacheRefreshWorker>(
            REFRESH_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CACHE_REFRESH_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            refreshRequest
        )
    }
    
    /**
     * Schedule periodic cache optimization using WorkManager
     */
    private fun schedulePeriodicOptimization() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .setRequiresDeviceIdle(true)
            .build()
        
        val optimizationRequest = PeriodicWorkRequestBuilder<CacheOptimizationWorker>(
            OPTIMIZATION_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            CACHE_OPTIMIZATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            optimizationRequest
        )
    }
    
    /**
     * Perform initial cleanup on app start
     */
    private suspend fun performInitialCleanup() {
        withContext(Dispatchers.IO) {
            try {
                // Clean up expired entries
                val expiredBreeds = breedDao.deleteExpiredBreeds()
                val expiredImages = imageCacheDao.deleteExpiredImages()
                
                if (expiredBreeds > 0 || expiredImages > 0) {
                    android.util.Log.d("CacheManager", "Initial cleanup: $expiredBreeds breeds, $expiredImages images")
                    
                    // Record statistics
                    val today = CacheStatsEntity.getTodayDateString()
                    cacheStatsDao.recordExpiredItems(today, CacheStatsEntity.CacheType.COMBINED, expiredBreeds + expiredImages)
                }
                
                // Clean up old statistics (keep last 90 days)
                cacheStatsDao.cleanupOldStats(90)
                
            } catch (e: Exception) {
                android.util.Log.w("CacheManager", "Initial cleanup failed", e)
            }
        }
    }
    
    /**
     * Manually trigger cache cleanup
     */
    suspend fun triggerCleanup(): CacheCleanupResult {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Clean up expired entries
                val expiredBreeds = breedDao.deleteExpiredBreeds()
                val expiredImages = imageCacheDao.deleteExpiredImages()
                
                // Clean up old statistics
                cacheStatsDao.cleanupOldStats(90)
                
                // Get current cache statistics
                val breedCount = breedDao.getValidBreedCount()
                val imageCount = imageCacheDao.getValidImageCount()
                val cacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                
                val duration = System.currentTimeMillis() - startTime
                
                CacheCleanupResult(
                    success = true,
                    expiredBreedsRemoved = expiredBreeds,
                    expiredImagesRemoved = expiredImages,
                    remainingBreeds = breedCount,
                    remainingImages = imageCount,
                    totalCacheSize = cacheSize,
                    cleanupDurationMs = duration
                )
                
            } catch (e: Exception) {
                android.util.Log.e("CacheManager", "Manual cleanup failed", e)
                CacheCleanupResult(
                    success = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Manually trigger cache refresh
     */
    suspend fun triggerRefresh(): CacheRefreshResult {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                
                // Get items near expiration
                val breedsNearExpiration = breedDao.getBreedsNearExpiration()
                val imagesNearExpiration = imageCacheDao.getImagesNearExpiration()
                
                // Perform background refresh
                cacheRepository.performBackgroundRefresh()
                
                val duration = System.currentTimeMillis() - startTime
                
                CacheRefreshResult(
                    success = true,
                    breedsRefreshed = breedsNearExpiration.size,
                    imagesRefreshed = imagesNearExpiration.size,
                    refreshDurationMs = duration
                )
                
            } catch (e: Exception) {
                android.util.Log.e("CacheManager", "Manual refresh failed", e)
                CacheRefreshResult(
                    success = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Manually trigger cache optimization
     */
    suspend fun triggerOptimization(): CacheOptimizationResult {
        return withContext(Dispatchers.IO) {
            try {
                val startTime = System.currentTimeMillis()
                val initialCacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                
                // Perform optimization
                cacheRepository.optimizeCache()
                
                // Check if we need to remove excess breeds
                val totalBreeds = breedDao.getTotalBreedCount()
                var breedsRemoved = 0
                
                if (totalBreeds > MAX_TOTAL_BREEDS) {
                    val excessBreeds = totalBreeds - MAX_TOTAL_BREEDS
                    // Remove least accessed breeds (this would need additional DAO method)
                    // For now, we'll just log the need for optimization
                    android.util.Log.d("CacheManager", "Need to remove $excessBreeds excess breeds")
                }
                
                val finalCacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                val spaceSaved = initialCacheSize - finalCacheSize
                val duration = System.currentTimeMillis() - startTime
                
                CacheOptimizationResult(
                    success = true,
                    initialCacheSize = initialCacheSize,
                    finalCacheSize = finalCacheSize,
                    spaceSaved = spaceSaved,
                    breedsRemoved = breedsRemoved,
                    optimizationDurationMs = duration
                )
                
            } catch (e: Exception) {
                android.util.Log.e("CacheManager", "Manual optimization failed", e)
                CacheOptimizationResult(
                    success = false,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Get comprehensive cache health report
     */
    suspend fun getCacheHealthReport(): CacheHealthReport {
        return withContext(Dispatchers.IO) {
            try {
                val breedStats = breedDao.getCacheStatistics()
                val imageStats = imageCacheDao.getImageCacheStatistics()
                val cacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                
                // Calculate health metrics
                val totalItems = breedStats.totalBreeds + imageStats.totalImages
                val validItems = breedStats.validBreeds + imageStats.validImages
                val expiredItems = breedStats.expiredBreeds + imageStats.expiredImages
                
                val healthScore = if (totalItems > 0) {
                    (validItems.toFloat() / totalItems * 100).toInt()
                } else {
                    100
                }
                
                val cacheUtilization = (cacheSize.toFloat() / (MAX_CACHE_SIZE_MB * 1024 * 1024) * 100).toInt()
                
                CacheHealthReport(
                    healthScore = healthScore,
                    totalBreeds = breedStats.totalBreeds,
                    validBreeds = breedStats.validBreeds,
                    expiredBreeds = breedStats.expiredBreeds,
                    totalImages = imageStats.totalImages,
                    validImages = imageStats.validImages,
                    expiredImages = imageStats.expiredImages,
                    totalCacheSize = cacheSize,
                    cacheUtilization = cacheUtilization,
                    oldestCacheTime = minOf(breedStats.oldestCacheTime, imageStats.oldestCacheTime),
                    newestCacheTime = maxOf(breedStats.newestCacheTime, imageStats.newestCacheTime),
                    recommendedActions = generateRecommendedActions(
                        healthScore, cacheUtilization, expiredItems, totalItems
                    )
                )
                
            } catch (e: Exception) {
                android.util.Log.e("CacheManager", "Failed to generate health report", e)
                CacheHealthReport(
                    healthScore = 0,
                    error = e.message
                )
            }
        }
    }
    
    /**
     * Generate recommended actions based on cache health
     */
    private fun generateRecommendedActions(
        healthScore: Int,
        cacheUtilization: Int,
        expiredItems: Int,
        totalItems: Int
    ): List<String> {
        val actions = mutableListOf<String>()
        
        if (healthScore < 70) {
            actions.add("Cache health is low. Consider refreshing expired data.")
        }
        
        if (cacheUtilization > 80) {
            actions.add("Cache is near capacity. Consider clearing old data.")
        }
        
        if (expiredItems > totalItems * 0.3) {
            actions.add("Many items have expired. Run cleanup to free space.")
        }
        
        if (totalItems == 0) {
            actions.add("Cache is empty. Consider pre-loading popular breeds.")
        }
        
        if (actions.isEmpty()) {
            actions.add("Cache is healthy. No action needed.")
        }
        
        return actions
    }
    
    /**
     * Cancel all scheduled cache management tasks
     */
    fun cancelAllTasks() {
        WorkManager.getInstance(context).apply {
            cancelUniqueWork(CACHE_CLEANUP_WORK_NAME)
            cancelUniqueWork(CACHE_REFRESH_WORK_NAME)
            cancelUniqueWork(CACHE_OPTIMIZATION_WORK_NAME)
        }
    }
}

// ===== DATA CLASSES FOR RESULTS =====

data class CacheCleanupResult(
    val success: Boolean,
    val expiredBreedsRemoved: Int = 0,
    val expiredImagesRemoved: Int = 0,
    val remainingBreeds: Int = 0,
    val remainingImages: Int = 0,
    val totalCacheSize: Long = 0L,
    val cleanupDurationMs: Long = 0L,
    val error: String? = null
)

data class CacheRefreshResult(
    val success: Boolean,
    val breedsRefreshed: Int = 0,
    val imagesRefreshed: Int = 0,
    val refreshDurationMs: Long = 0L,
    val error: String? = null
)

data class CacheOptimizationResult(
    val success: Boolean,
    val initialCacheSize: Long = 0L,
    val finalCacheSize: Long = 0L,
    val spaceSaved: Long = 0L,
    val breedsRemoved: Int = 0,
    val optimizationDurationMs: Long = 0L,
    val error: String? = null
)

data class CacheHealthReport(
    val healthScore: Int,
    val totalBreeds: Int = 0,
    val validBreeds: Int = 0,
    val expiredBreeds: Int = 0,
    val totalImages: Int = 0,
    val validImages: Int = 0,
    val expiredImages: Int = 0,
    val totalCacheSize: Long = 0L,
    val cacheUtilization: Int = 0,
    val oldestCacheTime: Long = 0L,
    val newestCacheTime: Long = 0L,
    val recommendedActions: List<String> = emptyList(),
    val error: String? = null
)