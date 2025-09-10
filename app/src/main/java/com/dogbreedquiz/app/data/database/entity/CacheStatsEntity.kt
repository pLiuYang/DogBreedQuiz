package com.dogbreedquiz.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for tracking cache statistics and performance metrics
 */
@Entity(
    tableName = "cache_stats",
    indices = [
        Index(value = ["date"]),
        Index(value = ["cache_type"])
    ]
)
data class CacheStatsEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "date")
    val date: String, // Date in YYYY-MM-DD format
    
    @ColumnInfo(name = "cache_type")
    val cacheType: CacheType,
    
    @ColumnInfo(name = "cache_hits")
    val cacheHits: Int = 0, // Number of successful cache retrievals
    
    @ColumnInfo(name = "cache_misses")
    val cacheMisses: Int = 0, // Number of cache misses requiring API calls
    
    @ColumnInfo(name = "api_calls")
    val apiCalls: Int = 0, // Number of API calls made
    
    @ColumnInfo(name = "bytes_cached")
    val bytesCached: Long = 0L, // Total bytes cached
    
    @ColumnInfo(name = "items_cached")
    val itemsCached: Int = 0, // Number of items cached
    
    @ColumnInfo(name = "items_expired")
    val itemsExpired: Int = 0, // Number of items that expired
    
    @ColumnInfo(name = "cache_cleared_count")
    val cacheClearedCount: Int = 0, // Number of times cache was manually cleared
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long = System.currentTimeMillis()
) {
    
    enum class CacheType {
        BREEDS,
        IMAGES,
        COMBINED
    }
    
    companion object {
        /**
         * Create daily stats entry
         */
        fun createDaily(date: String, cacheType: CacheType): CacheStatsEntity {
            return CacheStatsEntity(
                id = "${date}_${cacheType.name}",
                date = date,
                cacheType = cacheType
            )
        }
        
        /**
         * Get today's date string
         */
        fun getTodayDateString(): String {
            val calendar = java.util.Calendar.getInstance()
            return String.format(
                "%04d-%02d-%02d",
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH) + 1,
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
            )
        }
    }
    
    /**
     * Calculate cache hit rate
     */
    fun getCacheHitRate(): Float {
        val totalRequests = cacheHits + cacheMisses
        return if (totalRequests > 0) cacheHits.toFloat() / totalRequests else 0f
    }
    
    /**
     * Calculate API call reduction percentage
     */
    fun getApiReductionRate(): Float {
        val totalRequests = cacheHits + cacheMisses
        return if (totalRequests > 0) cacheHits.toFloat() / totalRequests else 0f
    }
    
    /**
     * Update stats with cache hit
     */
    fun withCacheHit(): CacheStatsEntity {
        return copy(
            cacheHits = cacheHits + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Update stats with cache miss
     */
    fun withCacheMiss(): CacheStatsEntity {
        return copy(
            cacheMisses = cacheMisses + 1,
            apiCalls = apiCalls + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Update stats with new cached item
     */
    fun withNewCachedItem(bytes: Long = 0L): CacheStatsEntity {
        return copy(
            itemsCached = itemsCached + 1,
            bytesCached = bytesCached + bytes,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Update stats with expired item
     */
    fun withExpiredItem(): CacheStatsEntity {
        return copy(
            itemsExpired = itemsExpired + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    /**
     * Update stats with cache clear
     */
    fun withCacheCleared(): CacheStatsEntity {
        return copy(
            cacheClearedCount = cacheClearedCount + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }
}