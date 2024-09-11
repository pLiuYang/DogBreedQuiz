package com.dogbreedquiz.app.domain.model

/**
 * Domain model for a dog breed
 * This is the core business entity representing a dog breed
 */
data class DogBreed(
    val id: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val funFact: String,
    val origin: String,
    val size: Size,
    val temperament: List<String>,
    val lifeSpan: String,
    val difficulty: Difficulty,
    val isFavorite: Boolean = false
) {
    enum class Size {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }
    
    enum class Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
    
    /**
     * Check if this breed has a valid image URL
     */
    fun hasImage(): Boolean = imageUrl.isNotEmpty()
    
    /**
     * Get display name with proper formatting
     */
    fun getDisplayName(): String = name
    
    /**
     * Check if this breed matches a search query
     */
    fun matchesQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return name.lowercase().contains(lowerQuery) ||
                origin.lowercase().contains(lowerQuery) ||
                temperament.any { it.lowercase().contains(lowerQuery) }
    }
}