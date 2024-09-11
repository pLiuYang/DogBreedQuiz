package com.dogbreedquiz.app.domain.repository

/**
 * Repository interface for cache management operations
 * Provides abstraction for different caching strategies
 */
interface CacheRepository {
    
    /**
     * Clear all cached data
     */
    suspend fun clearAllCache()
    
    /**
     * Clear expired cache entries
     */
    suspend fun clearExpiredCache()
    
    /**
     * Get cache statistics
     * @return Cache statistics summary
     */
    suspend fun getCacheStatistics(): CacheStatistics
    
    /**
     * Perform background cache refresh
     */
    suspend fun performBackgroundRefresh()
    
    /**
     * Optimize cache by removing least accessed items
     */
    suspend fun optimizeCache()
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