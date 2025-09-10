
package com.dogbreedquiz.app.di

import com.dogbreedquiz.app.data.api.DogApiService
import com.dogbreedquiz.app.data.api.repository.DogApiRepository
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.repository.DogBreedCacheRepository
import com.dogbreedquiz.app.data.repository.DogBreedRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies with Room database caching
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    /**
     * Provides DogApiRepository instance
     */
    @Provides
    @Singleton
    fun provideDogApiRepository(
        dogApiService: DogApiService
    ): DogApiRepository {
        return DogApiRepository(dogApiService)
    }
    
    /**
     * Provides DogBreedCacheRepository instance with database caching
     */
    @Provides
    @Singleton
    fun provideDogBreedCacheRepository(
        dogApiRepository: DogApiRepository,
        breedDao: BreedDao,
        imageCacheDao: ImageCacheDao,
        cacheStatsDao: CacheStatsDao
    ): DogBreedCacheRepository {
        return DogBreedCacheRepository(
            dogApiRepository = dogApiRepository,
            breedDao = breedDao,
            imageCacheDao = imageCacheDao,
            cacheStatsDao = cacheStatsDao
        )
    }
    
    /**
     * Provides DogBreedRepository instance (delegates to cache repository)
     */
    @Provides
    @Singleton
    fun provideDogBreedRepository(
        cacheRepository: DogBreedCacheRepository
    ): DogBreedRepository {
        return DogBreedRepository(cacheRepository)
    }
}