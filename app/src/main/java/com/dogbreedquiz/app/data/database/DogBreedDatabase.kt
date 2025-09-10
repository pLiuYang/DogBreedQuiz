package com.dogbreedquiz.app.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.data.database.entity.CacheStatsEntity
import com.dogbreedquiz.app.data.database.entity.ImageCacheEntity

/**
 * Room database for Dog Breed Quiz app with comprehensive caching functionality
 */
@Database(
    entities = [
        BreedEntity::class,
        ImageCacheEntity::class,
        CacheStatsEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DatabaseConverters::class)
abstract class DogBreedDatabase : RoomDatabase() {
    
    // ===== DAO PROVIDERS =====
    
    /**
     * Provides access to breed-related database operations
     */
    abstract fun breedDao(): BreedDao
    
    /**
     * Provides access to image cache operations
     */
    abstract fun imageCacheDao(): ImageCacheDao
    
    /**
     * Provides access to cache statistics operations
     */
    abstract fun cacheStatsDao(): CacheStatsDao
    
    companion object {
        const val DATABASE_NAME = "dog_breed_quiz_database"
        const val DATABASE_VERSION = 1
        
        /**
         * Create database instance with proper configuration
         */
        fun create(context: android.content.Context): DogBreedDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                DogBreedDatabase::class.java,
                DATABASE_NAME
            )
                .addCallback(DatabaseCallback())
                .addMigrations(*getAllMigrations())
                .fallbackToDestructiveMigration() // For development - remove in production
                .build()
        }
        
        /**
         * Get all database migrations
         */
        private fun getAllMigrations(): Array<Migration> {
            return arrayOf(
                // Future migrations will be added here
            )
        }
    }
}

/**
 * Database callback for initialization and cleanup tasks
 */
private class DatabaseCallback : RoomDatabase.Callback() {
    
    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        
        // Create indexes for better performance
        createAdditionalIndexes(db)
        
        // Initialize cache statistics table with today's entry
        initializeCacheStats(db)
    }
    
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)
        
        // Enable foreign key constraints
        db.execSQL("PRAGMA foreign_keys = ON")
        
        // Optimize database performance
        db.execSQL("PRAGMA journal_mode = WAL")
        db.execSQL("PRAGMA synchronous = NORMAL")
        db.execSQL("PRAGMA cache_size = 10000")
        db.execSQL("PRAGMA temp_store = MEMORY")
        
        // Clean up expired entries on database open
        cleanupExpiredEntries(db)
    }
    
    /**
     * Create additional indexes for optimal query performance
     */
    private fun createAdditionalIndexes(db: SupportSQLiteDatabase) {
        // Composite indexes for common query patterns
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_breeds_difficulty_expires 
            ON breeds(difficulty, expires_at)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_breeds_size_expires 
            ON breeds(size, expires_at)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_image_cache_breed_type_expires 
            ON image_cache(breed_id, image_type, expires_at)
        """)
        
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_image_cache_primary_expires 
            ON image_cache(is_primary, expires_at)
        """)
        
        // Full-text search index for breed names and descriptions
        db.execSQL("""
            CREATE INDEX IF NOT EXISTS idx_breeds_search 
            ON breeds(name, origin, temperament)
        """)
    }
    
    /**
     * Initialize cache statistics with today's entries
     */
    private fun initializeCacheStats(db: SupportSQLiteDatabase) {
        val today = getCurrentDateString()
        val currentTime = System.currentTimeMillis()
        
        // Insert initial stats for all cache types
        val cacheTypes = listOf("BREEDS", "IMAGES", "COMBINED")
        
        cacheTypes.forEach { cacheType ->
            db.execSQL("""
                INSERT OR IGNORE INTO cache_stats 
                (id, date, cache_type, last_updated) 
                VALUES (?, ?, ?, ?)
            """, arrayOf("${today}_$cacheType", today, cacheType, currentTime))
        }
    }
    
    /**
     * Clean up expired entries on database open
     */
    private fun cleanupExpiredEntries(db: SupportSQLiteDatabase) {
        val currentTime = System.currentTimeMillis()
        
        // Clean up expired breeds
        db.execSQL("DELETE FROM breeds WHERE expires_at < ?", arrayOf(currentTime))
        
        // Clean up expired images
        db.execSQL("DELETE FROM image_cache WHERE expires_at < ?", arrayOf(currentTime))
        
        // Clean up old statistics (keep last 90 days)
        val cutoffDate = getDateStringDaysAgo(90)
        db.execSQL("DELETE FROM cache_stats WHERE date < ?", arrayOf(cutoffDate))
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