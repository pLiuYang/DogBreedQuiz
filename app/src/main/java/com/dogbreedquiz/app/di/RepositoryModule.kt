package com.dogbreedquiz.app.di

import com.dogbreedquiz.app.data.api.DogApiService
import com.dogbreedquiz.app.data.api.repository.DogApiClient
import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.dao.CacheStatsDao
import com.dogbreedquiz.app.data.database.dao.ImageCacheDao
import com.dogbreedquiz.app.data.datasource.local.LocalBreedDataSource
import com.dogbreedquiz.app.data.datasource.local.LocalBreedDataSourceImpl
import com.dogbreedquiz.app.data.datasource.remote.RemoteBreedDataSource
import com.dogbreedquiz.app.data.datasource.remote.RemoteBreedDataSourceImpl
import com.dogbreedquiz.app.data.mapper.BreedMapper
import com.dogbreedquiz.app.data.mapper.BreedMapperImpl
import com.dogbreedquiz.app.data.mapper.DogBreedMapper
import com.dogbreedquiz.app.data.repository.DogBreedCacheRepository
import com.dogbreedquiz.app.data.repository.DogBreedRepositoryImpl
import com.dogbreedquiz.app.data.repository.QuizRepositoryImpl
import com.dogbreedquiz.app.domain.repository.CacheRepository
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import com.dogbreedquiz.app.domain.repository.QuizRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies with clean architecture
 * Uses interfaces to abstract implementation details and improve testability
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    // ===== ABSTRACT BINDINGS =====
    
    /**
     * Bind DogBreedRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindDogBreedRepository(
        dogBreedRepositoryImpl: DogBreedRepositoryImpl
    ): DogBreedRepository
    
    /**
     * Bind QuizRepository interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindQuizRepository(
        quizRepositoryImpl: QuizRepositoryImpl
    ): QuizRepository
    
    /**
     * Bind LocalBreedDataSource interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindLocalBreedDataSource(
        localBreedDataSourceImpl: LocalBreedDataSourceImpl
    ): LocalBreedDataSource
    
    /**
     * Bind RemoteBreedDataSource interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindRemoteBreedDataSource(
        remoteBreedDataSourceImpl: RemoteBreedDataSourceImpl
    ): RemoteBreedDataSource
    
    /**
     * Bind BreedMapper interface to its implementation
     */
    @Binds
    @Singleton
    abstract fun bindBreedMapper(
        breedMapperImpl: BreedMapperImpl
    ): BreedMapper
    
    /**
     * Bind CacheRepository interface to existing cache implementation
     */
    @Binds
    @Singleton
    abstract fun bindCacheRepository(
        dogBreedCacheRepository: DogBreedCacheRepository
    ): CacheRepository
    
    companion object {
        
        // ===== CONCRETE PROVIDERS =====
        
        /**
         * Provides DogApiRepository instance
         */
        @Provides
        @Singleton
        fun provideDogApiRepository(
            dogApiService: DogApiService
        ): DogApiClient {
            return DogApiClient(dogApiService)
        }
        
        /**
         * Provides DogBreedCacheRepository instance with database caching
         * This is kept for backward compatibility and cache management
         */
        @Provides
        @Singleton
        fun provideDogBreedCacheRepository(
            dogApiClient: DogApiClient,
            breedDao: BreedDao,
            imageCacheDao: ImageCacheDao,
            cacheStatsDao: CacheStatsDao,
            dogBreedMapper: DogBreedMapper
        ): DogBreedCacheRepository {
            return DogBreedCacheRepository(
                dogApiClient = dogApiClient,
                breedDao = breedDao,
                imageCacheDao = imageCacheDao,
                cacheStatsDao = cacheStatsDao,
                mapper = dogBreedMapper
            )
        }
    }
}