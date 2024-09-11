package com.dogbreedquiz.app.data.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.dogbreedquiz.app.domain.model.DogBreed

/**
 * Room entity for storing dog breed information with cache expiration
 */
@Entity(
    tableName = "breeds",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["cached_at"]),
        Index(value = ["expires_at"]),
        Index(value = ["difficulty"]),
        Index(value = ["size"])
    ]
)
data class BreedEntity(
    @PrimaryKey
    val id: String,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "image_url")
    val imageUrl: String,
    
    @ColumnInfo(name = "description")
    val description: String,
    
    @ColumnInfo(name = "fun_fact")
    val funFact: String,
    
    @ColumnInfo(name = "origin")
    val origin: String,
    
    @ColumnInfo(name = "size")
    val size: String, // Store as string for Room compatibility
    
    @ColumnInfo(name = "temperament")
    val temperament: String, // Store as comma-separated string
    
    @ColumnInfo(name = "life_span")
    val lifeSpan: String,
    
    @ColumnInfo(name = "difficulty")
    val difficulty: String, // Store as string for Room compatibility
    
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long, // Timestamp when data was cached
    
    @ColumnInfo(name = "expires_at")
    val expiresAt: Long, // Timestamp when cache expires (7 days from cached_at)
    
    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long, // Timestamp when data was last updated from API
    
    @ColumnInfo(name = "api_source")
    val apiSource: String = "dog.ceo", // Source of the data
    
    @ColumnInfo(name = "is_favorite")
    val isFavorite: Boolean = false // User favorite status
) {
    companion object {
        const val CACHE_DURATION_DAYS = 7L
        const val CACHE_DURATION_MS = CACHE_DURATION_DAYS * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        
        /**
         * Create BreedEntity from DogBreed model
         */
        fun fromDogBreed(dogBreed: DogBreed): BreedEntity {
            val currentTime = System.currentTimeMillis()
            return BreedEntity(
                id = dogBreed.id,
                name = dogBreed.name,
                imageUrl = dogBreed.imageUrl,
                description = dogBreed.description,
                funFact = dogBreed.funFact,
                origin = dogBreed.origin,
                size = dogBreed.size.name,
                temperament = dogBreed.temperament.joinToString(","),
                lifeSpan = dogBreed.lifeSpan,
                difficulty = dogBreed.difficulty.name,
                cachedAt = currentTime,
                expiresAt = currentTime + CACHE_DURATION_MS,
                lastUpdated = currentTime
            )
        }
    }
    
    /**
     * Convert BreedEntity to DogBreed model
     */
    fun toDogBreed(): DogBreed {
        return DogBreed(
            id = id,
            name = name,
            imageUrl = imageUrl,
            description = description,
            funFact = funFact,
            origin = origin,
            size = DogBreed.Size.valueOf(size),
            temperament = temperament.split(",").map { it.trim() },
            lifeSpan = lifeSpan,
            difficulty = DogBreed.Difficulty.valueOf(difficulty),
            isFavorite = isFavorite
        )
    }
    
    /**
     * Check if the cached data is still valid (not expired)
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
}