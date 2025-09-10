package com.dogbreedquiz.app.data.cache

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import com.dogbreedquiz.app.data.repository.DogBreedCacheRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker for periodic cache cleanup operations
 */
@HiltWorker
class CacheCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val breedDao: BreedDao,
    private val imageCacheDao: ImageCacheDao,
    private val cacheStatsDao: CacheStatsDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CacheCleanupWorker", "Starting cache cleanup")
                
                // Clean up expired breeds
                val expiredBreeds = breedDao.deleteExpiredBreeds()
                
                // Clean up expired images
                val expiredImages = imageCacheDao.deleteExpiredImages()
                
                // Clean up old statistics (keep last 90 days)
                cacheStatsDao.cleanupOldStats(90)
                
                // Record cleanup statistics
                if (expiredBreeds > 0 || expiredImages > 0) {
                    val today = CacheStatsEntity.getTodayDateString()
                    cacheStatsDao.recordExpiredItems(
                        today, 
                        CacheStatsEntity.CacheType.COMBINED, 
                        expiredBreeds + expiredImages
                    )
                }
                
                android.util.Log.d(
                    "CacheCleanupWorker", 
                    "Cleanup completed: $expiredBreeds breeds, $expiredImages images removed"
                )
                
                Result.success()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheCleanupWorker", "Cache cleanup failed", e)
                Result.retry()
            }
        }
    }
}

/**
 * Worker for periodic cache refresh operations
 */
@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheRepository: DogBreedCacheRepository,
    private val breedDao: BreedDao,
    private val imageCacheDao: ImageCacheDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CacheRefreshWorker", "Starting background cache refresh")
                
                // Get items near expiration
                val breedsNearExpiration = breedDao.getBreedsNearExpiration()
                val imagesNearExpiration = imageCacheDao.getImagesNearExpiration()
                
                val totalItemsToRefresh = breedsNearExpiration.size + imagesNearExpiration.size
                
                if (totalItemsToRefresh > 0) {
                    // Perform background refresh
                    cacheRepository.performBackgroundRefresh()
                    
                    android.util.Log.d(
                        "CacheRefreshWorker", 
                        "Refresh completed: $totalItemsToRefresh items refreshed"
                    )
                } else {
                    android.util.Log.d("CacheRefreshWorker", "No items need refreshing")
                }
                
                Result.success()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheRefreshWorker", "Cache refresh failed", e)
                // For refresh operations, we can retry on failure
                Result.retry()
            }
        }
    }
}

/**
 * Worker for periodic cache optimization operations
 */
@HiltWorker
class CacheOptimizationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheRepository: DogBreedCacheRepository,
    private val imageCacheDao: ImageCacheDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val MAX_CACHE_SIZE_MB = 100L
    }

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CacheOptimizationWorker", "Starting cache optimization")
                
                val initialCacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                val maxCacheSizeBytes = MAX_CACHE_SIZE_MB * 1024 * 1024
                
                // Perform optimization
                cacheRepository.optimizeCache()
                
                val finalCacheSize = imageCacheDao.getTotalCacheSize() ?: 0L
                val spaceSaved = initialCacheSize - finalCacheSize
                
                android.util.Log.d(
                    "CacheOptimizationWorker", 
                    "Optimization completed: ${spaceSaved / 1024 / 1024}MB saved"
                )
                
                // Additional optimization if still over limit
                if (finalCacheSize > maxCacheSizeBytes) {
                    val excessSize = finalCacheSize - maxCacheSizeBytes
                    val itemsToDelete = (excessSize / (1024 * 1024)).toInt() + 5
                    
                    imageCacheDao.deleteLeastAccessedImages(itemsToDelete)
                    
                    android.util.Log.d(
                        "CacheOptimizationWorker", 
                        "Additional cleanup: $itemsToDelete least accessed images removed"
                    )
                }
                
                Result.success()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheOptimizationWorker", "Cache optimization failed", e)
                // Optimization failures are not critical, so we succeed anyway
                Result.success()
            }
        }
    }
}

/**
 * Worker for one-time cache initialization operations
 */
@HiltWorker
class CacheInitializationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheRepository: DogBreedCacheRepository,
    private val cacheStatsDao: CacheStatsDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CacheInitializationWorker", "Starting cache initialization")
                
                // Initialize cache with popular breeds
                val breeds = cacheRepository.getAllBreeds(forceRefresh = false)
                
                // Initialize today's statistics if they don't exist
                val today = CacheStatsEntity.getTodayDateString()
                val cacheTypes = listOf(
                    CacheStatsEntity.CacheType.BREEDS,
                    CacheStatsEntity.CacheType.IMAGES,
                    CacheStatsEntity.CacheType.COMBINED
                )
                
                cacheTypes.forEach { cacheType ->
                    if (!cacheStatsDao.statsExist(today, cacheType)) {
                        cacheStatsDao.insertStats(CacheStatsEntity.createDaily(today, cacheType))
                    }
                }
                
                android.util.Log.d(
                    "CacheInitializationWorker", 
                    "Initialization completed: ${breeds.size} breeds loaded"
                )
                
                Result.success()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheInitializationWorker", "Cache initialization failed", e)
                // Initialization failure should be retried
                Result.retry()
            }
        }
    }
}

/**
 * Worker for cache statistics aggregation
 */
@HiltWorker
class CacheStatsAggregationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val cacheStatsDao: CacheStatsDao,
    private val breedDao: BreedDao,
    private val imageCacheDao: ImageCacheDao
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("CacheStatsAggregationWorker", "Starting statistics aggregation")
                
                val today = CacheStatsEntity.getTodayDateString()
                
                // Aggregate breed statistics
                val breedStats = breedDao.getCacheStatistics()
                val breedStatsEntity = cacheStatsDao.getStatsForDate(today, CacheStatsEntity.CacheType.BREEDS)
                
                if (breedStatsEntity != null) {
                    val updatedBreedStats = breedStatsEntity.copy(
                        itemsCached = breedStats.totalBreeds,
                        itemsExpired = breedStats.expiredBreeds,
                        lastUpdated = System.currentTimeMillis()
                    )
                    cacheStatsDao.updateStats(updatedBreedStats)
                }
                
                // Aggregate image statistics
                val imageStats = imageCacheDao.getImageCacheStatistics()
                val imageStatsEntity = cacheStatsDao.getStatsForDate(today, CacheStatsEntity.CacheType.IMAGES)
                
                if (imageStatsEntity != null) {
                    val updatedImageStats = imageStatsEntity.copy(
                        itemsCached = imageStats.totalImages,
                        itemsExpired = imageStats.expiredImages,
                        bytesCached = imageStats.totalSizeBytes,
                        lastUpdated = System.currentTimeMillis()
                    )
                    cacheStatsDao.updateStats(updatedImageStats)
                }
                
                // Aggregate combined statistics
                val combinedStatsEntity = cacheStatsDao.getStatsForDate(today, CacheStatsEntity.CacheType.COMBINED)
                
                if (combinedStatsEntity != null) {
                    val updatedCombinedStats = combinedStatsEntity.copy(
                        itemsCached = breedStats.totalBreeds + imageStats.totalImages,
                        itemsExpired = breedStats.expiredBreeds + imageStats.expiredImages,
                        bytesCached = imageStats.totalSizeBytes,
                        lastUpdated = System.currentTimeMillis()
                    )
                    cacheStatsDao.updateStats(updatedCombinedStats)
                }
                
                android.util.Log.d("CacheStatsAggregationWorker", "Statistics aggregation completed")
                
                Result.success()
                
            } catch (e: Exception) {
                android.util.Log.e("CacheStatsAggregationWorker", "Statistics aggregation failed", e)
                // Statistics aggregation failure is not critical
                Result.success()
            }
        }
    }
}