
package com.dogbreedquiz.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.dogbreedquiz.app.data.cache.CacheManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class for Dog Breed Quiz App with comprehensive caching support
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection
 */
@HiltAndroidApp
class DogBreedQuizApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var cacheManager: CacheManager
    
    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize cache management system
        initializeCacheSystem()
    }
    
    /**
     * Provide WorkManager configuration with Hilt worker factory
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    
    /**
     * Initialize the comprehensive caching system
     */
    private fun initializeCacheSystem() {
        try {
            // Initialize cache manager with background tasks
            cacheManager.initialize()
            
            android.util.Log.i("DogBreedQuizApp", "Cache management system initialized successfully")
            
        } catch (e: Exception) {
            android.util.Log.e("DogBreedQuizApp", "Failed to initialize cache system", e)
        }
    }
}