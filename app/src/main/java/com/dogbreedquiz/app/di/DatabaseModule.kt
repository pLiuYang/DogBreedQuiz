package com.dogbreedquiz.app.di

import android.content.Context
import androidx.room.Room
import com.dogbreedquiz.app.data.database.DogBreedDatabase
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    /**
     * Provides the main Room database instance
     */
    @Provides
    @Singleton
    fun provideDogBreedDatabase(
        @ApplicationContext context: Context
    ): DogBreedDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            DogBreedDatabase::class.java,
            DogBreedDatabase.DATABASE_NAME
        )
            .addCallback(DatabaseCallback(context))
            .fallbackToDestructiveMigration() // For development - remove in production
            .build()
    }
    
    /**
     * Provides BreedDao for breed-related database operations
     */
    @Provides
    fun provideBreedDao(database: DogBreedDatabase): BreedDao {
        return database.breedDao()
    }
    
    /**
     * Provides ImageCacheDao for image cache operations
     */
    @Provides
    fun provideImageCacheDao(database: DogBreedDatabase): ImageCacheDao {
        return database.imageCacheDao()
    }
    
    /**
     * Provides CacheStatsDao for cache statistics operations
     */
    @Provides
    fun provideCacheStatsDao(database: DogBreedDatabase): CacheStatsDao {
        return database.cacheStatsDao()
    }
}

/**
 * Database callback for initialization and maintenance tasks
 */
private class DatabaseCallback(
    private val context: Context
) : androidx.room.RoomDatabase.Callback() {
    
    override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON")
        
        // Create additional indexes for better performance
        createPerformanceIndexes(db)
        
        // Initialize cache statistics
        initializeCacheStatistics(db)
    }
    
    override fun onOpen(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        super.onOpen(db)
        
        // Ensure foreign key constraints are enabled
        db.execSQL("PRAGMA foreign_keys = ON")
        
        // Perform maintenance tasks
        performMaintenanceTasks(db)
    }
    
    /**
     * Create additional indexes for optimal query performance
     */
    private fun createPerformanceIndexes(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // Basic indexes for common query patterns
        val indexes = listOf(
            "CREATE INDEX IF NOT EXISTS idx_breeds_difficulty ON breeds(difficulty)",
            "CREATE INDEX IF NOT EXISTS idx_breeds_expires ON breeds(expires_at)",
            "CREATE INDEX IF NOT EXISTS idx_image_cache_breed ON image_cache(breed_id)",
            "CREATE INDEX IF NOT EXISTS idx_image_cache_expires ON image_cache(expires_at)",
            "CREATE INDEX IF NOT EXISTS idx_cache_stats_date ON cache_stats(date)"
        )
        
        indexes.forEach { indexSql ->
            try {
                db.execSQL(indexSql)
            } catch (e: Exception) {
                // Log error but continue with other indexes
                android.util.Log.w("DatabaseModule", "Failed to create index: $indexSql", e)
            }
        }
    }
    
    /**
     * Initialize cache statistics with today's entries
     */
    private fun initializeCacheStatistics(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        val today = getCurrentDateString()
        val currentTime = System.currentTimeMillis()
        
        // Insert initial stats for all cache types
        val cacheTypes = listOf("BREEDS", "IMAGES", "COMBINED")
        
        cacheTypes.forEach { cacheType ->
            try {
                val id = "${today}_$cacheType"
                val sql = "INSERT OR IGNORE INTO cache_stats (id, date, cache_type, cache_hits, cache_misses, api_calls, bytes_cached, items_cached, items_expired, cache_cleared_count, last_updated) VALUES (?, ?, ?, 0, 0, 0, 0, 0, 0, 0, ?)"
                db.execSQL(sql, arrayOf(id, today, cacheType, currentTime))
            } catch (e: Exception) {
                android.util.Log.w("DatabaseModule", "Failed to initialize cache stats for $cacheType", e)
            }
        }
    }
    
    /**
     * Perform regular maintenance tasks
     */
    private fun performMaintenanceTasks(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        val currentTime = System.currentTimeMillis()
        
        try {
            // Clean up expired breeds
            db.execSQL("DELETE FROM breeds WHERE expires_at < ?", arrayOf(currentTime.toString()))
            
            // Clean up expired images
            db.execSQL("DELETE FROM image_cache WHERE expires_at < ?", arrayOf(currentTime.toString()))
            
            // Clean up old statistics (keep last 90 days)
            val cutoffDate = getDateStringDaysAgo(90)
            db.execSQL("DELETE FROM cache_stats WHERE date < ?", arrayOf(cutoffDate))
            
        } catch (e: Exception) {
            android.util.Log.w("DatabaseModule", "Error during maintenance tasks", e)
        }
    }
    
    /**
     * Get current date string in YYYY-MM-DD format
     */
    private fun getCurrentDateString(): String {
        val calendar = java.util.Calendar.getInstance()
        return String.format(
            "%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
    
    /**
     * Get date string for N days ago
     */
    private fun getDateStringDaysAgo(daysAgo: Int): String {
        val calendar = java.util.Calendar.getInstance()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -daysAgo)
        return String.format(
            "%04d-%02d-%02d",
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH) + 1,
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
    }
}