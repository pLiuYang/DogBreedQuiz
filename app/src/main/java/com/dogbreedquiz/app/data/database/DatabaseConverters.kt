package com.dogbreedquiz.app.data.database

import androidx.room.TypeConverter
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import com.dogbreedquiz.app.data.database.entity.ImageCacheEntity

/**
 * Type converters for Room database to handle complex data types
 */
class DatabaseConverters {
    
    // ===== ENUM CONVERTERS =====
    
    /**
     * Convert ImageType enum to string for database storage
     */
    @TypeConverter
    fun fromImageType(imageType: ImageCacheEntity.ImageType): String {
        return imageType.name
    }
    
    /**
     * Convert string to ImageType enum from database
     */
    @TypeConverter
    fun toImageType(imageType: String): ImageCacheEntity.ImageType {
        return try {
            ImageCacheEntity.ImageType.valueOf(imageType)
        } catch (e: IllegalArgumentException) {
            ImageCacheEntity.ImageType.PRIMARY // Default fallback
        }
    }
    
    /**
     * Convert CacheType enum to string for database storage
     */
    @TypeConverter
    fun fromCacheType(cacheType: CacheStatsEntity.CacheType): String {
        return cacheType.name
    }
    
    /**
     * Convert string to CacheType enum from database
     */
    @TypeConverter
    fun toCacheType(cacheType: String): CacheStatsEntity.CacheType {
        return try {
            CacheStatsEntity.CacheType.valueOf(cacheType)
        } catch (e: IllegalArgumentException) {
            CacheStatsEntity.CacheType.COMBINED // Default fallback
        }
    }
    
    // ===== LIST CONVERTERS =====
    
    /**
     * Convert list of strings to comma-separated string for database storage
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return value.joinToString(",")
    }
    
    /**
     * Convert comma-separated string to list of strings from database
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",").map { it.trim() }
        }
    }
    
    /**
     * Convert list of integers to comma-separated string for database storage
     */
    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return value.joinToString(",")
    }
    
    /**
     * Convert comma-separated string to list of integers from database
     */
    @TypeConverter
    fun toIntList(value: String): List<Int> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",").mapNotNull { it.trim().toIntOrNull() }
        }
    }
    
    /**
     * Convert list of longs to comma-separated string for database storage
     */
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return value.joinToString(",")
    }
    
    /**
     * Convert comma-separated string to list of longs from database
     */
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return if (value.isBlank()) {
            emptyList()
        } else {
            value.split(",").mapNotNull { it.trim().toLongOrNull() }
        }
    }
    
    // ===== MAP CONVERTERS =====
    
    /**
     * Convert map of string to string to JSON-like format for database storage
     */
    @TypeConverter
    fun fromStringMap(value: Map<String, String>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
    
    /**
     * Convert JSON-like string to map of string to string from database
     */
    @TypeConverter
    fun toStringMap(value: String): Map<String, String> {
        return if (value.isBlank()) {
            emptyMap()
        } else {
            value.split(";").mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) {
                    parts[0].trim() to parts[1].trim()
                } else {
                    null
                }
            }.toMap()
        }
    }
    
    /**
     * Convert map of string to int to JSON-like format for database storage
     */
    @TypeConverter
    fun fromStringIntMap(value: Map<String, Int>): String {
        return value.entries.joinToString(";") { "${it.key}:${it.value}" }
    }
    
    /**
     * Convert JSON-like string to map of string to int from database
     */
    @TypeConverter
    fun toStringIntMap(value: String): Map<String, Int> {
        return if (value.isBlank()) {
            emptyMap()
        } else {
            value.split(";").mapNotNull { entry ->
                val parts = entry.split(":", limit = 2)
                if (parts.size == 2) {
                    val key = parts[0].trim()
                    val intValue = parts[1].trim().toIntOrNull()
                    if (intValue != null) {
                        key to intValue
                    } else {
                        null
                    }
                } else {
                    null
                }
            }.toMap()
        }
    }
    
    // ===== DATE/TIME CONVERTERS =====
    
    /**
     * Convert timestamp to formatted date string for display
     */
    fun timestampToDateString(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return String.format(
            "%04d-%02d-%02d %02d:%02d:%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH),
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            calendar.get(java.util.Calendar.SECOND)
        )
    }
    
    /**
     * Convert timestamp to date-only string
     */
    fun timestampToDateOnlyString(timestamp: Long): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return String.format(
            "%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    
    /**
     * Get current timestamp
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }
    
    /**
     * Check if timestamp is within the last N days
     */
    fun isWithinLastDays(timestamp: Long, days: Int): Boolean {
        val cutoffTime = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
        return timestamp >= cutoffTime
    }
    
    /**
     * Get timestamp for N days from now
     */
    fun getTimestampDaysFromNow(days: Int): Long {
        return System.currentTimeMillis() + (days * 24 * 60 * 60 * 1000L)
    }
    
    /**
     * Get timestamp for N days ago
     */
    fun getTimestampDaysAgo(days: Int): Long {
        return System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000L)
    }
    
    // ===== UTILITY CONVERTERS =====
    
    /**
     * Convert boolean to integer for database storage (SQLite compatibility)
     */
    @TypeConverter
    fun fromBoolean(value: Boolean): Int {
        return if (value) 1 else 0
    }
    
    /**
     * Convert integer to boolean from database
     */
    @TypeConverter
    fun toBoolean(value: Int): Boolean {
        return value != 0
    }
    
    /**
     * Convert nullable string to non-null string for database
     */
    @TypeConverter
    fun fromNullableString(value: String?): String {
        return value ?: ""
    }
    
    /**
     * Convert string to nullable string from database
     */
    @TypeConverter
    fun toNullableString(value: String): String? {
        return if (value.isBlank()) null else value
    }
    
    /**
     * Format bytes to human-readable string
     */
    fun formatBytes(bytes: Long): String {
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        var size = bytes.toDouble()
        var unitIndex = 0
        
        while (size >= 1024 && unitIndex < units.size - 1) {
            size /= 1024
            unitIndex++
        }
        
        return String.format("%.2f %s", size, units[unitIndex])
    }
    
    /**
     * Format percentage with proper precision
     */
    fun formatPercentage(value: Double): String {
        return String.format("%.1f%%", value * 100)
    }
    
    /**
     * Format duration in milliseconds to human-readable string
     */
    fun formatDuration(durationMs: Long): String {
        val seconds = durationMs / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        
        return when {
            days > 0 -> "${days}d ${hours % 24}h"
            hours > 0 -> "${hours}h ${minutes % 60}m"
            minutes > 0 -> "${minutes}m ${seconds % 60}s"
            else -> "${seconds}s"
        }
    }
}