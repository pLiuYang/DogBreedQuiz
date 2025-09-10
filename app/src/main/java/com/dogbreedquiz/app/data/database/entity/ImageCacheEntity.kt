package com.dogbreedquiz.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity for storing cached breed images with expiration logic
 */
@Entity(
    tableName = "image_cache",
    indices = [
        Index(value = ["breed_id"]),
        Index(value = ["cached_at"]),
        Index(value = ["expires_at"]),
        Index(value = ["image_type"]),
        Index(value = ["is_primary"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = BreedEntity::class,
            parentColumns = ["id"],
            childColumns = ["breed_id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ImageCacheEntity(
    @PrimaryKey
    val id: String, // Unique identifier for the cached image
    
    @ColumnInfo(name = "breed_id")
    val breedId: String, // Foreign key to BreedEntity
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String, // The cached image URL
    
    @ColumnInfo(name = "image_type")
    val imageType: ImageType, // Type of image (primary, quiz, gallery)
    
    @ColumnInfo(name = "is_primary")
    val isPrimary: Boolean = false, // Whether this is the primary image for the breed
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long, // Timestamp when image was cached
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long, // Timestamp when cache expires
    
    @ColumnInfo(name = "last_accessed")
    val lastAccessed: Long, // Timestamp when image was last accessed
    
    @ColumnInfo(name = "access_count")
    val accessCount: Int = 0, // Number of times image has been accessed
    
    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0L, // Size of cached image file in bytes
    
    @ColumnInfo(name = "local_path")
    val localPath: String? = null // Local file path if image is downloaded
) {
    
    enum class ImageType {
        PRIMARY,    // Main breed image
        QUIZ,       // Image used in quiz questions
        GALLERY,    // Additional breed images
        THUMBNAIL   // Thumbnail version
    }
    
    companion object {
        const val CACHE_DURATION_DAYS = 7L
        const val CACHE_DURATION_MS = CACHE_DURATION_DAYS * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        
        /**
         * Create ImageCacheEntity for a breed image
         */
        fun create(
            breedId: String,
            imageUrl: String,
            imageType: ImageType = ImageType.PRIMARY,
            isPrimary: Boolean = false
        ): ImageCacheEntity {
            val currentTime = System.currentTimeMillis()
            return ImageCacheEntity(
                id = generateImageId(breedId, imageUrl, imageType),
                breedId = breedId,
                imageUrl = imageUrl,
                imageType = imageType,
                isPrimary = isPrimary,
                cachedAt = currentTime,
                expiresAt = currentTime + CACHE_DURATION_MS,
                lastAccessed = currentTime
            )
        }
        
        /**
         * Generate unique ID for image cache entry
         */
        private fun generateImageId(breedId: String, imageUrl: String, imageType: ImageType): String {
            return "${breedId}_${imageType.name}_${imageUrl.hashCode()}"
        }
    }
    
    /**
     * Check if the cached image is still valid (not expired)
     */
    fun isValid(): Boolean {
        return System.currentTimeMillis() < expiresAt
    }
    
    /**
     * Check if the cache is near expiration (within 1 day)
     */
    fun isNearExpiration(): Boolean {
        val oneDayMs = 24 * 60 * 60 * 1000L
        return System.currentTimeMillis() > (expiresAt - oneDayMs)
    }
    
    /**
     * Create updated entity with new access information
     */
    fun withAccess(): ImageCacheEntity {
        return copy(
            lastAccessed = System.currentTimeMillis(),
            accessCount = accessCount + 1
        )
    }
    
    /**
     * Create updated entity with extended expiration
     */
    fun withExtendedExpiration(): ImageCacheEntity {
        val currentTime = System.currentTimeMillis()
        return copy(
            expiresAt = currentTime + CACHE_DURATION_MS,
            lastAccessed = currentTime
        )
    }
}