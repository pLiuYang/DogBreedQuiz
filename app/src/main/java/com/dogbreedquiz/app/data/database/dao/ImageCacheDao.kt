package com.dogbreedquiz.app.data.database.dao

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.dogbreedquiz.app.data.database.entity.ImageCacheEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for image cache operations
 */
@Dao
interface ImageCacheDao {
    
    // ===== INSERT OPERATIONS =====
    
    /**
     * Insert a single cached image, replacing if it already exists
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageCacheEntity): Long
    
    /**
     * Insert multiple cached images, replacing if they already exist
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImages(images: List<ImageCacheEntity>): List<Long>
    
    // ===== UPDATE OPERATIONS =====
    
    /**
     * Update an existing cached image
     */
    @Update
    suspend fun updateImage(image: ImageCacheEntity): Int
    
    /**
     * Update multiple cached images
     */
    @Update
    suspend fun updateImages(images: List<ImageCacheEntity>): Int
    
    /**
     * Update image access information
     */
    @Query("UPDATE image_cache SET last_accessed = :lastAccessed, access_count = access_count + 1 WHERE id = :imageId")
    suspend fun updateImageAccess(imageId: String, lastAccessed: Long = System.currentTimeMillis()): Int
    
    /**
     * Update image expiration time (extend cache)
     */
    @Query("UPDATE image_cache SET expires_at = :expiresAt WHERE id = :imageId")
    suspend fun extendImageExpiration(imageId: String, expiresAt: Long): Int
    
    /**
     * Set primary image for a breed (unset others first)
     */
    @Transaction
    suspend fun setPrimaryImage(breedId: String, imageId: String) {
        // Unset all primary flags for this breed
        unsetPrimaryImagesForBreed(breedId)
        // Set the specified image as primary
        setPrimaryImageFlag(imageId, true)
    }
    
    @Query("UPDATE image_cache SET is_primary = 0 WHERE breed_id = :breedId")
    suspend fun unsetPrimaryImagesForBreed(breedId: String): Int
    
    @Query("UPDATE image_cache SET is_primary = :isPrimary WHERE id = :imageId")
    suspend fun setPrimaryImageFlag(imageId: String, isPrimary: Boolean): Int
    
    // ===== DELETE OPERATIONS =====
    
    /**
     * Delete a specific cached image
     */
    @Delete
    suspend fun deleteImage(image: ImageCacheEntity): Int
    
    /**
     * Delete cached image by ID
     */
    @Query("DELETE FROM image_cache WHERE id = :imageId")
    suspend fun deleteImageById(imageId: String): Int
    
    /**
     * Delete all cached images for a specific breed
     */
    @Query("DELETE FROM image_cache WHERE breed_id = :breedId")
    suspend fun deleteImagesForBreed(breedId: String): Int
    
    /**
     * Delete all expired images
     */
    @Query("DELETE FROM image_cache WHERE expires_at < :currentTime")
    suspend fun deleteExpiredImages(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Delete images by type
     */
    @Query("DELETE FROM image_cache WHERE image_type = :imageType")
    suspend fun deleteImagesByType(imageType: ImageCacheEntity.ImageType): Int
    
    /**
     * Delete least recently accessed images to free space
     */
    @Query("DELETE FROM image_cache WHERE id IN (SELECT id FROM image_cache ORDER BY last_accessed ASC LIMIT :count)")
    suspend fun deleteLeastAccessedImages(count: Int): Int
    
    /**
     * Delete all cached images (clear cache)
     */
    @Query("DELETE FROM image_cache")
    suspend fun deleteAllImages(): Int
    
    // ===== SELECT OPERATIONS =====
    
    /**
     * Get all cached images
     */
    @Query("SELECT * FROM image_cache ORDER BY cached_at DESC")
    suspend fun getAllImages(): List<ImageCacheEntity>
    
    /**
     * Get all valid (non-expired) images
     */
    @Query("SELECT * FROM image_cache WHERE expires_at > :currentTime ORDER BY cached_at DESC")
    suspend fun getValidImages(currentTime: Long = System.currentTimeMillis()): List<ImageCacheEntity>
    
    /**
     * Get cached image by ID
     */
    @Query("SELECT * FROM image_cache WHERE id = :imageId")
    suspend fun getImageById(imageId: String): ImageCacheEntity?
    
    /**
     * Get all images for a specific breed
     */
    @Query("SELECT * FROM image_cache WHERE breed_id = :breedId AND expires_at > :currentTime ORDER BY is_primary DESC, cached_at DESC")
    suspend fun getImagesForBreed(
        breedId: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<ImageCacheEntity>
    
    /**
     * Get images for breed as Flow
     */
    @Query("SELECT * FROM image_cache WHERE breed_id = :breedId AND expires_at > :currentTime ORDER BY is_primary DESC, cached_at DESC")
    fun getImagesForBreedFlow(
        breedId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<List<ImageCacheEntity>>
    
    /**
     * Get primary image for a breed
     */
    @Query("SELECT * FROM image_cache WHERE breed_id = :breedId AND is_primary = 1 AND expires_at > :currentTime LIMIT 1")
    suspend fun getPrimaryImageForBreed(
        breedId: String,
        currentTime: Long = System.currentTimeMillis()
    ): ImageCacheEntity?
    
    /**
     * Get primary image for breed as Flow
     */
    @Query("SELECT * FROM image_cache WHERE breed_id = :breedId AND is_primary = 1 AND expires_at > :currentTime LIMIT 1")
    fun getPrimaryImageForBreedFlow(
        breedId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Flow<ImageCacheEntity?>
    
    /**
     * Get images by type
     */
    @Query("SELECT * FROM image_cache WHERE image_type = :imageType AND expires_at > :currentTime ORDER BY cached_at DESC")
    suspend fun getImagesByType(
        imageType: ImageCacheEntity.ImageType,
        currentTime: Long = System.currentTimeMillis()
    ): List<ImageCacheEntity>
    
    /**
     * Get images for quiz (random selection)
     */
    @Query("SELECT * FROM image_cache WHERE image_type IN ('PRIMARY', 'QUIZ') AND expires_at > :currentTime ORDER BY RANDOM() LIMIT :count")
    suspend fun getRandomImagesForQuiz(
        count: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<ImageCacheEntity>
    
    /**
     * Get images that are near expiration (for background refresh)
     */
    @Query("SELECT * FROM image_cache WHERE expires_at > :currentTime AND expires_at < :nearExpirationTime ORDER BY expires_at ASC")
    suspend fun getImagesNearExpiration(
        currentTime: Long = System.currentTimeMillis(),
        nearExpirationTime: Long = System.currentTimeMillis() + (24 * 60 * 60 * 1000L) // 1 day
    ): List<ImageCacheEntity>
    
    /**
     * Get most accessed images
     */
    @Query("SELECT * FROM image_cache WHERE expires_at > :currentTime ORDER BY access_count DESC LIMIT :count")
    suspend fun getMostAccessedImages(
        count: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<ImageCacheEntity>
    
    /**
     * Search cached images by breed name or URL
     */
    @Query("""
        SELECT ic.* FROM image_cache ic
        INNER JOIN breeds b ON ic.breed_id = b.id
        WHERE (b.name LIKE '%' || :query || '%' OR ic.image_url LIKE '%' || :query || '%')
        AND ic.expires_at > :currentTime
        ORDER BY ic.cached_at DESC
    """)
    suspend fun searchImages(
        query: String,
        currentTime: Long = System.currentTimeMillis()
    ): List<ImageCacheEntity>
    
    // ===== COUNT OPERATIONS =====
    
    /**
     * Get total count of cached images
     */
    @Query("SELECT COUNT(*) FROM image_cache")
    suspend fun getTotalImageCount(): Int
    
    /**
     * Get count of valid (non-expired) images
     */
    @Query("SELECT COUNT(*) FROM image_cache WHERE expires_at > :currentTime")
    suspend fun getValidImageCount(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Get count of expired images
     */
    @Query("SELECT COUNT(*) FROM image_cache WHERE expires_at <= :currentTime")
    suspend fun getExpiredImageCount(currentTime: Long = System.currentTimeMillis()): Int
    
    /**
     * Get count of images for a specific breed
     */
    @Query("SELECT COUNT(*) FROM image_cache WHERE breed_id = :breedId AND expires_at > :currentTime")
    suspend fun getImageCountForBreed(
        breedId: String,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Get count of images by type
     */
    @Query("SELECT COUNT(*) FROM image_cache WHERE image_type = :imageType AND expires_at > :currentTime")
    suspend fun getImageCountByType(
        imageType: ImageCacheEntity.ImageType,
        currentTime: Long = System.currentTimeMillis()
    ): Int
    
    /**
     * Get total cache size in bytes
     */
    @Query("SELECT SUM(file_size) FROM image_cache WHERE expires_at > :currentTime")
    suspend fun getTotalCacheSize(currentTime: Long = System.currentTimeMillis()): Long?
    
    // ===== UTILITY OPERATIONS =====
    
    /**
     * Check if image exists in cache
     */
    @Query("SELECT EXISTS(SELECT 1 FROM image_cache WHERE id = :imageId)")
    suspend fun imageExists(imageId: String): Boolean
    
    /**
     * Check if image is valid (not expired)
     */
    @Query("SELECT EXISTS(SELECT 1 FROM image_cache WHERE id = :imageId AND expires_at > :currentTime)")
    suspend fun isImageValid(imageId: String, currentTime: Long = System.currentTimeMillis()): Boolean
    
    /**
     * Check if breed has cached images
     */
    @Query("SELECT EXISTS(SELECT 1 FROM image_cache WHERE breed_id = :breedId AND expires_at > :currentTime)")
    suspend fun breedHasCachedImages(breedId: String, currentTime: Long = System.currentTimeMillis()): Boolean
    
    /**
     * Check if breed has primary image
     */
    @Query("SELECT EXISTS(SELECT 1 FROM image_cache WHERE breed_id = :breedId AND is_primary = 1 AND expires_at > :currentTime)")
    suspend fun breedHasPrimaryImage(breedId: String, currentTime: Long = System.currentTimeMillis()): Boolean
    
    /**
     * Get image cache statistics
     */
    @Query("""
        SELECT 
            COUNT(*) as total_images,
            COUNT(CASE WHEN expires_at > :currentTime THEN 1 END) as valid_images,
            COUNT(CASE WHEN expires_at <= :currentTime THEN 1 END) as expired_images,
            COUNT(CASE WHEN is_primary = 1 THEN 1 END) as primary_images,
            COUNT(DISTINCT breed_id) as breeds_with_images,
            SUM(file_size) as total_size_bytes,
            SUM(access_count) as total_access_count,
            AVG(access_count) as average_access_count,
            MIN(cached_at) as oldest_cache_time,
            MAX(cached_at) as newest_cache_time
        FROM image_cache
    """)
    suspend fun getImageCacheStatistics(currentTime: Long = System.currentTimeMillis()): ImageCacheStatistics
    
    /**
     * Transaction to refresh image cache for a breed
     */
    @Transaction
    suspend fun refreshBreedImageCache(breedId: String, newImages: List<ImageCacheEntity>) {
        // Delete existing images for the breed
        deleteImagesForBreed(breedId)
        // Insert new images
        insertImages(newImages)
    }
    
    /**
     * Transaction to cleanup expired images and optimize cache
     */
    @Transaction
    suspend fun cleanupAndOptimizeCache(maxCacheSize: Long) {
        // Delete expired images first
        deleteExpiredImages()
        
        // Check current cache size
        val currentSize = getTotalCacheSize() ?: 0L
        
        // If still over limit, delete least accessed images
        if (currentSize > maxCacheSize) {
            val imagesToDelete = ((currentSize - maxCacheSize) / 1024 / 1024).toInt() + 10 // Rough estimate
            deleteLeastAccessedImages(imagesToDelete)
        }
    }
}

/**
 * Data class for image cache statistics
 */
data class ImageCacheStatistics(
    @ColumnInfo(name = "total_images")
    val totalImages: Int,
    @ColumnInfo(name = "valid_images")
    val validImages: Int,
    @ColumnInfo(name = "expired_images")
    val expiredImages: Int,
    @ColumnInfo(name = "primary_images")
    val primaryImages: Int,
    @ColumnInfo(name = "breeds_with_images")
    val breedsWithImages: Int,
    @ColumnInfo(name = "total_size_bytes")
    val totalSizeBytes: Long,
    @ColumnInfo(name = "total_access_count")
    val totalAccessCount: Int,
    @ColumnInfo(name = "average_access_count")
    val averageAccessCount: Double,
    @ColumnInfo(name = "oldest_cache_time")
    val oldestCacheTime: Long,
    @ColumnInfo(name = "newest_cache_time")
    val newestCacheTime: Long
)