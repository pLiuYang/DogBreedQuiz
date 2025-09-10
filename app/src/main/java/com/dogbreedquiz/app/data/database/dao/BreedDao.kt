package com.dogbreedquiz.app.data.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.data.model.DogBreed
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for breed-related database operations
 */
@Dao
interface BreedDao {
    
    // ===== INSERT OPERATIONS =====
    
    /**
     * Insert a single breed, replacing if it already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreed(breed: BreedEntity): Long
    
    /**
     * Insert multiple breeds, replacing if they already exist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBreeds(breeds: List<BreedEntity>): List<Long>
    
    // ===== UPDATE OPERATIONS =====
    
    /**
     * Update an existing breed
     */
    @Update
    suspend fun updateBreed(breed: BreedEntity): Int
    
    /**
     * Update multiple breeds
     */
    @Update
    suspend fun updateBreeds(breeds: List<BreedEntity>): Int
    
    /**
     * Update breed's favorite status
     */
    @Query("UPDATE breeds SET is_favorite = :isFavorite WHERE id = :breedId")
    suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean): Int
    
    /**
     * Update breed's expiration time (extend cache)
     */
    @Query("UPDATE breeds SET expires_at = :expiresAt, last_updated = :lastUpdated WHERE id = :breedId")
    suspend fun extendCacheExpiration(breedId: String, expiresAt: Long, lastUpdated: Long): Int
    
    // ===== DELETE OPERATIONS =====
    
    /**
     * Delete a specific breed
     */
    @Delete
    suspend fun deleteBreed(breed: BreedEntity): Int
    
    /**
     * Delete breed by ID
     */
    @Query("DELETE FROM breeds WHERE id = :breedId")
    suspend fun deleteBreedById(breedId: String): Int
    
    /**
     * Delete all expired breeds
     */
    @Query("DELETE FROM breeds WHERE expires_at < :currentTime")
    suspend fun deleteExpiredBreeds(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Delete all breeds (clear cache)
     */
    @Query("DELETE FROM breeds")
    suspend fun deleteAllBreeds(): Int
    
    // ===== SELECT OPERATIONS =====
    
    /**
     * Get all breeds (including expired ones)
     */
    @Query("SELECT * FROM breeds ORDER BY name ASC")
    suspend fun getAllBreeds(): List<BreedEntity>
    
    /**
     * Get all valid (non-expired) breeds
     */
    @Query("SELECT * FROM breeds WHERE expires_at > :currentTime ORDER BY name ASC")
    suspend fun getValidBreeds(currentTime: Long = System.currentTimeMillis()): List<BreedEntity>
    
    /**
     * Get all valid breeds as Flow for reactive updates
     */
    @Query("SELECT * FROM breeds WHERE expires_at > :currentTime ORDER BY name ASC")
    fun getValidBreedsFlow(currentTime: Long = System.currentTimeMillis()): Flow<List<BreedEntity>>
    
    /**
     * Get breed by ID
     */
    @Query("SELECT * FROM breeds WHERE id = :breedId")
    suspend fun getBreedById(breedId: String): BreedEntity?
    
    /**
     * Get breed by ID as Flow
     */
    @Query("SELECT * FROM breeds WHERE id = :breedId")
    fun getBreedByIdFlow(breedId: String): Flow<BreedEntity?>
    
    /**
     * Get breeds by difficulty level
     */
    @Query("SELECT * FROM breeds WHERE difficulty = :difficulty AND expires_at > :currentTime ORDER BY name ASC")
    suspend fun getBreedsByDifficulty(
        difficulty: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<BreedEntity>
    
    /**
     * Get breeds by size
     */
    @Query("SELECT * FROM breeds WHERE size = :size AND expires_at > :currentTime ORDER BY name ASC")
    suspend fun getBreedsBySize(
        size: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<BreedEntity>
    
    /**
     * Search breeds by name or origin
     */
    @Query("""
        SELECT * FROM breeds 
        WHERE (name LIKE '%' || :query || '%' OR origin LIKE '%' || :query || '%' OR temperament LIKE '%' || :query || '%')
        AND expires_at > :currentTime 
        ORDER BY name ASC
    """)
    suspend fun searchBreeds(
        query: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<BreedEntity>
    
    /**
     * Get favorite breeds
     */
    @Query("SELECT * FROM breeds WHERE is_favorite = 1 AND expires_at > :currentTime ORDER BY name ASC")
    suspend fun getFavoriteBreeds(currentTime: Long = System.currentTimeMillis()): List<BreedEntity>
    
    /**
     * Get favorite breeds as Flow
     */
    @Query("SELECT * FROM breeds WHERE is_favorite = 1 AND expires_at > :currentTime ORDER BY name ASC")
    fun getFavoriteBreedsFlow(currentTime: Long = System.currentTimeMillis()): Flow<List<BreedEntity>>
    
    /**
     * Get random breeds for quiz generation
     */
    @Query("SELECT * FROM breeds WHERE expires_at > :currentTime ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomBreeds(
        count: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<BreedEntity>
    
    /**
     * Get breeds that are near expiration (for background refresh)
     */
    @Query("SELECT * FROM breeds WHERE expires_at > :currentTime AND expires_at < :nearExpirationTime ORDER BY expires_at ASC")
    suspend fun getBreedsNearExpiration(
        currentTime: Long = System.currentTimeMillis(),
        nearExpirationTime: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 1 day
    ): List<BreedEntity>
    
    // ===== COUNT OPERATIONS =====
    
    /**
     * Get total count of cached breeds
     */
    @Query("SELECT COUNT(*) FROM breeds")
    suspend fun getTotalBreedCount(): Int
    
    /**
     * Get count of valid (non-expired) breeds
     */
    @Query("SELECT COUNT(*) FROM breeds WHERE expires_at > :currentTime")
    suspend fun getValidBreedCount(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Get count of expired breeds
     */
    @Query("SELECT COUNT(*) FROM breeds WHERE expires_at <= :currentTime")
    suspend fun getExpiredBreedCount(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Get count of favorite breeds
     */
    @Query("SELECT COUNT(*) FROM breeds WHERE is_favorite = 1 AND expires_at > :currentTime")
    suspend fun getFavoriteBreedCount(currentTime: Long = System.currentTimeMillis()): Int
    
    // ===== UTILITY OPERATIONS =====
    
    /**
     * Check if breed exists in cache
     */
    @Query("SELECT EXISTS(SELECT 1 FROM breeds WHERE id = :breedId)")
    suspend fun breedExists(breedId: String): Boolean
    
    /**
     * Check if breed is valid (not expired)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM breeds WHERE id = :breedId AND expires_at > :currentTime)")
    suspend fun isBreedValid(breedId: String, currentTime: Long = System.currentTimeMillis()): Boolean
    
    /**
     * Get cache statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as total_breeds,
            COUNT(CASE WHEN expires_at > :currentTime THEN 1 END) as valid_breeds,
            COUNT(CASE WHEN expires_at <= :currentTime THEN 1 END) as expired_breeds,
            COUNT(CASE WHEN is_favorite = 1 THEN 1 END) as favorite_breeds,
            MIN(cached_at) as oldest_cache_time,
            MAX(cached_at) as newest_cache_time,
            AVG(cached_at) as average_cache_time
        FROM breeds
    """)
    suspend fun getCacheStatistics(currentTime: Long = System.currentTimeMillis()): CacheStatistics
    
    /**
     * Transaction to refresh breed cache
     */
    @Transaction
    suspend fun refreshBreedCache(newBreeds: List<BreedEntity>) {
        // Delete expired breeds first
        deleteExpiredBreeds()
        // Insert new breeds
        insertBreeds(newBreeds)
    }
    
    /**
     * Transaction to clear and rebuild cache
     */
    @Transaction
    suspend fun rebuildCache(newBreeds: List<BreedEntity>) {
        // Clear all existing breeds
        deleteAllBreeds()
        // Insert new breeds
        insertBreeds(newBreeds)
    }
}

/**
 * Data class for cache statistics
 */
data class CacheStatistics(
    @ColumnInfo(name = "total_breeds")
    val totalBreeds: Int,
    @ColumnInfo(name = "valid_breeds")
    val validBreeds: Int,
    @ColumnInfo(name = "expired_breeds")
    val expiredBreeds: Int,
    @ColumnInfo(name = "favorite_breeds")
    val favoriteBreeds: Int,
    @ColumnInfo(name = "oldest_cache_time")
    val oldestCacheTime: Long,
    @ColumnInfo(name = "newest_cache_time")
    val newestCacheTime: Long,
    @ColumnInfo(name = "average_cache_time")
    val averageCacheTime: Long
)