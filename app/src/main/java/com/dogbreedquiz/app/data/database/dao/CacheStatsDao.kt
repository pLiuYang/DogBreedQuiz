package com.dogbreedquiz.app.data.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for cache statistics operations
 */
@Dao
interface CacheStatsDao {
    
    // ===== INSERT OPERATIONS =====
    
    /**
     * Insert cache statistics entry, replacing if it already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: CacheStatsEntity): Long
    
    /**
     * Insert multiple statistics entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleStats(stats: List<CacheStatsEntity>): List<Long>
    
    // ===== UPDATE OPERATIONS =====
    
    /**
     * Update existing statistics entry
     */
    @Update
    suspend fun updateStats(stats: CacheStatsEntity): Int
    
    /**
     * Increment cache hit count for today
     */
    @Query("""
        UPDATE cache_stats 
        SET cache_hits = cache_hits + 1, last_updated = :currentTime 
        WHERE date = :date AND cache_type = :cacheType
    """)
    suspend fun incrementCacheHit(
        date: String,
        cacheType: CacheStatsEntity.CacheType,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Increment cache miss count for today
     */
    @Query("""
        UPDATE cache_stats 
        SET cache_misses = cache_misses + 1, api_calls = api_calls + 1, last_updated = :currentTime 
        WHERE date = :date AND cache_type = :cacheType
    """)
    suspend fun incrementCacheMiss(
        date: String,
        cacheType: CacheStatsEntity.CacheType,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Add cached item statistics
     */
    @Query("""
        UPDATE cache_stats 
        SET items_cached = items_cached + 1, bytes_cached = bytes_cached + :bytes, last_updated = :currentTime 
        WHERE date = :date AND cache_type = :cacheType
    """)
    suspend fun addCachedItem(
        date: String,
        cacheType: CacheStatsEntity.CacheType,
        bytes: Long = 0L,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Increment expired items count
     */
    @Query("""
        UPDATE cache_stats 
        SET items_expired = items_expired + :count, last_updated = :currentTime 
        WHERE date = :date AND cache_type = :cacheType
    """)
    suspend fun incrementExpiredItems(
        date: String,
        cacheType: CacheStatsEntity.CacheType,
        count: Int = 1,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Increment cache cleared count
     */
    @Query("""
        UPDATE cache_stats 
        SET cache_cleared_count = cache_cleared_count + 1, last_updated = :currentTime 
        WHERE date = :date AND cache_type = :cacheType
    """)
    suspend fun incrementCacheCleared(
        date: String,
        cacheType: CacheStatsEntity.CacheType,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    // ===== DELETE OPERATIONS =====
    
    /**
     * Delete specific statistics entry
     */
    @Delete
    suspend fun deleteStats(stats: CacheStatsEntity): Int
    
    /**
     * Delete statistics by date
     */
    @Query("DELETE FROM cache_stats WHERE date = :date")
    suspend fun deleteStatsByDate(date: String): Int
    
    /**
     * Delete statistics older than specified date
     */
    @Query("DELETE FROM cache_stats WHERE date < :date")
    suspend fun deleteStatsOlderThan(date: String): Int
    
    /**
     * Delete all statistics (clear all)
     */
    @Query("DELETE FROM cache_stats")
    suspend fun deleteAllStats(): Int
    
    // ===== SELECT OPERATIONS =====
    
    /**
     * Get all statistics entries
     */
    @Query("SELECT * FROM cache_stats ORDER BY date DESC")
    suspend fun getAllStats(): List<CacheStatsEntity>
    
    /**
     * Get statistics for a specific date and cache type
     */
    @Query("SELECT * FROM cache_stats WHERE date = :date AND cache_type = :cacheType")
    suspend fun getStatsForDate(date: String, cacheType: CacheStatsEntity.CacheType): CacheStatsEntity?
    
    /**
     * Get statistics for a specific date and cache type as Flow
     */
    @Query("SELECT * FROM cache_stats WHERE date = :date AND cache_type = :cacheType")
    fun getStatsForDateFlow(date: String, cacheType: CacheStatsEntity.CacheType): Flow<CacheStatsEntity?>
    
    /**
     * Get today's statistics
     */
    @Query("SELECT * FROM cache_stats WHERE date = :today")
    suspend fun getTodayStats(today: String = CacheStatsEntity.getTodayDateString()): List<CacheStatsEntity>
    
    /**
     * Get today's statistics as Flow
     */
    @Query("SELECT * FROM cache_stats WHERE date = :today")
    fun getTodayStatsFlow(today: String = CacheStatsEntity.getTodayDateString()): Flow<List<CacheStatsEntity>>
    
    /**
     * Get statistics for date range
     */
    @Query("SELECT * FROM cache_stats WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getStatsForDateRange(startDate: String, endDate: String): List<CacheStatsEntity>
    
    /**
     * Get statistics for last N days
     */
    @Query("SELECT * FROM cache_stats WHERE date >= :startDate ORDER BY date DESC")
    suspend fun getStatsForLastDays(startDate: String): List<CacheStatsEntity>
    
    /**
     * Get statistics by cache type
     */
    @Query("SELECT * FROM cache_stats WHERE cache_type = :cacheType ORDER BY date DESC")
    suspend fun getStatsByCacheType(cacheType: CacheStatsEntity.CacheType): List<CacheStatsEntity>
    
    /**
     * Get statistics by cache type as Flow
     */
    @Query("SELECT * FROM cache_stats WHERE cache_type = :cacheType ORDER BY date DESC")
    fun getStatsByCacheTypeFlow(cacheType: CacheStatsEntity.CacheType): Flow<List<CacheStatsEntity>>
    
    /**
     * Get recent statistics (last 30 days)
     */
    @Query("SELECT * FROM cache_stats WHERE date >= date('now', '-30 days') ORDER BY date DESC")
    suspend fun getRecentStats(): List<CacheStatsEntity>
    
    /**
     * Get recent statistics as Flow
     */
    @Query("SELECT * FROM cache_stats WHERE date >= date('now', '-30 days') ORDER BY date DESC")
    fun getRecentStatsFlow(): Flow<List<CacheStatsEntity>>
    
    // ===== AGGREGATE OPERATIONS =====
    
    /**
     * Get total cache hits for date range
     */
    @Query("SELECT SUM(cache_hits) FROM cache_stats WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType")
    suspend fun getTotalCacheHits(startDate: String, endDate: String, cacheType: CacheStatsEntity.CacheType): Int?
    
    /**
     * Get total cache misses for date range
     */
    @Query("SELECT SUM(cache_misses) FROM cache_stats WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType")
    suspend fun getTotalCacheMisses(startDate: String, endDate: String, cacheType: CacheStatsEntity.CacheType): Int?
    
    /**
     * Get total API calls for date range
     */
    @Query("SELECT SUM(api_calls) FROM cache_stats WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType")
    suspend fun getTotalApiCalls(startDate: String, endDate: String, cacheType: CacheStatsEntity.CacheType): Int?
    
    /**
     * Get total bytes cached for date range
     */
    @Query("SELECT SUM(bytes_cached) FROM cache_stats WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType")
    suspend fun getTotalBytesCached(startDate: String, endDate: String, cacheType: CacheStatsEntity.CacheType): Long?
    
    /**
     * Get average cache hit rate for date range
     */
    @Query("""
        SELECT 
            CASE 
                WHEN SUM(cache_hits + cache_misses) > 0 
                THEN CAST(SUM(cache_hits) AS REAL) / SUM(cache_hits + cache_misses) 
                ELSE 0.0 
            END as hit_rate
        FROM cache_stats 
        WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType
    """)
    suspend fun getAverageCacheHitRate(startDate: String, endDate: String, cacheType: CacheStatsEntity.CacheType): Double?
    
    /**
     * Get comprehensive cache performance summary
     */
    @Query("""
        SELECT 
            COUNT(*) as total_days,
            SUM(cache_hits) as total_cache_hits,
            SUM(cache_misses) as total_cache_misses,
            SUM(api_calls) as total_api_calls,
            SUM(bytes_cached) as total_bytes_cached,
            SUM(items_cached) as total_items_cached,
            SUM(items_expired) as total_items_expired,
            SUM(cache_cleared_count) as total_cache_clears,
            AVG(cache_hits) as avg_daily_cache_hits,
            AVG(cache_misses) as avg_daily_cache_misses,
            AVG(api_calls) as avg_daily_api_calls,
            CASE 
                WHEN SUM(cache_hits + cache_misses) > 0 
                THEN CAST(SUM(cache_hits) AS REAL) / SUM(cache_hits + cache_misses) 
                ELSE 0.0 
            END as overall_hit_rate
        FROM cache_stats 
        WHERE date BETWEEN :startDate AND :endDate AND cache_type = :cacheType
    """)
    suspend fun getCachePerformanceSummary(
        startDate: String,
        endDate: String,
        cacheType: CacheStatsEntity.CacheType
    ): CachePerformanceSummary?
    
    // ===== UTILITY OPERATIONS =====
    
    /**
     * Check if statistics exist for a specific date and cache type
     */
    @Query("SELECT EXISTS(SELECT 1 FROM cache_stats WHERE date = :date AND cache_type = :cacheType)")
    suspend fun statsExist(date: String, cacheType: CacheStatsEntity.CacheType): Boolean
    
    /**
     * Get the earliest date with statistics
     */
    @Query("SELECT MIN(date) FROM cache_stats")
    suspend fun getEarliestStatsDate(): String?
    
    /**
     * Get the latest date with statistics
     */
    @Query("SELECT MAX(date) FROM cache_stats")
    suspend fun getLatestStatsDate(): String?
    
    /**
     * Get count of days with statistics
     */
    @Query("SELECT COUNT(DISTINCT date) FROM cache_stats")
    suspend fun getStatsDayCount(): Int
    
    // ===== TRANSACTION OPERATIONS =====
    
    /**
     * Record cache hit (create entry if doesn't exist)
     */
    @Transaction
    suspend fun recordCacheHit(date: String, cacheType: CacheStatsEntity.CacheType) {
        if (!statsExist(date, cacheType)) {
            insertStats(CacheStatsEntity.createDaily(date, cacheType))
        }
        incrementCacheHit(date, cacheType)
    }
    
    /**
     * Record cache miss (create entry if doesn't exist)
     */
    @Transaction
    suspend fun recordCacheMiss(date: String, cacheType: CacheStatsEntity.CacheType) {
        if (!statsExist(date, cacheType)) {
            insertStats(CacheStatsEntity.createDaily(date, cacheType))
        }
        incrementCacheMiss(date, cacheType)
    }
    
    /**
     * Record cached item (create entry if doesn't exist)
     */
    @Transaction
    suspend fun recordCachedItem(date: String, cacheType: CacheStatsEntity.CacheType, bytes: Long = 0L) {
        if (!statsExist(date, cacheType)) {
            insertStats(CacheStatsEntity.createDaily(date, cacheType))
        }
        addCachedItem(date, cacheType, bytes)
    }
    
    /**
     * Record expired items (create entry if doesn't exist)
     */
    @Transaction
    suspend fun recordExpiredItems(date: String, cacheType: CacheStatsEntity.CacheType, count: Int = 1) {
        if (!statsExist(date, cacheType)) {
            insertStats(CacheStatsEntity.createDaily(date, cacheType))
        }
        incrementExpiredItems(date, cacheType, count)
    }
    
    /**
     * Record cache cleared (create entry if doesn't exist)
     */
    @Transaction
    suspend fun recordCacheCleared(date: String, cacheType: CacheStatsEntity.CacheType) {
        if (!statsExist(date, cacheType)) {
            insertStats(CacheStatsEntity.createDaily(date, cacheType))
        }
        incrementCacheCleared(date, cacheType)
    }
    
    /**
     * Cleanup old statistics (keep only last N days)
     */
    @Transaction
    suspend fun cleanupOldStats(keepDays: Int = 90) {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -keepDays)
        val cutoffDate = String.format(
            "%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        deleteStatsOlderThan(cutoffDate)
    }
}

/**
 * Data class for cache performance summary
 */
data class CachePerformanceSummary(
    @ColumnInfo(name = "total_days")
    val totalDays: Int,
    @ColumnInfo(name = "total_cache_hits")
    val totalCacheHits: Int,
    @ColumnInfo(name = "total_cache_misses")
    val totalCacheMisses: Int,
    @ColumnInfo(name = "total_api_calls")
    val totalApiCalls: Int,
    @ColumnInfo(name = "total_bytes_cached")
    val totalBytesCached: Long,
    @ColumnInfo(name = "total_items_cached")
    val totalItemsCached: Int,
    @ColumnInfo(name = "total_items_expired")
    val totalItemsExpired: Int,
    @ColumnInfo(name = "total_cache_clears")
    val totalCacheClears: Int,
    @ColumnInfo(name = "avg_daily_cache_hits")
    val avgDailyCacheHits: Double,
    @ColumnInfo(name = "avg_daily_cache_misses")
    val avgDailyCacheMisses: Double,
    @ColumnInfo(name = "avg_daily_api_calls")
    val avgDailyApiCalls: Double,
    @ColumnInfo(name = "overall_hit_rate")
    val overallHitRate: Double
)