package com.dogbreedquiz.app.di

import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import com.dogbreedquiz.app.domain.repository.QuizRepository
import com.dogbreedquiz.app.domain.usecase.GenerateQuizUseCase
import com.dogbreedquiz.app.domain.usecase.GetAllBreedsUseCase
import com.dogbreedquiz.app.domain.usecase.GetQuizStatisticsUseCase
import com.dogbreedquiz.app.domain.usecase.LoadBreedImageUseCase
import com.dogbreedquiz.app.domain.usecase.SaveQuizSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing use case dependencies
 * Centralizes use case creation and dependency management
 */
@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    /**
     * Provides GetAllBreedsUseCase instance
     */
    @Provides
    @Singleton
    fun provideGetAllBreedsUseCase(
        repository: DogBreedRepository
    ): GetAllBreedsUseCase {
        return GetAllBreedsUseCase(repository)
    }
    
    /**
     * Provides GenerateQuizUseCase instance
     */
    @Provides
    @Singleton
    fun provideGenerateQuizUseCase(
        repository: DogBreedRepository
    ): GenerateQuizUseCase {
        return GenerateQuizUseCase(repository)
    }
    
    /**
     * Provides LoadBreedImageUseCase instance
     */
    @Provides
    @Singleton
    fun provideLoadBreedImageUseCase(
        repository: DogBreedRepository
    ): LoadBreedImageUseCase {
        return LoadBreedImageUseCase(repository)
    }
    
    /**
     * Provides SaveQuizSessionUseCase instance
     */
    @Provides
    @Singleton
    fun provideSaveQuizSessionUseCase(
        repository: QuizRepository
    ): SaveQuizSessionUseCase {
        return SaveQuizSessionUseCase(repository)
    }
    
    /**
     * Provides GetQuizStatisticsUseCase instance
     */
    @Provides
    @Singleton
    fun provideGetQuizStatisticsUseCase(
        repository: QuizRepository
    ): GetQuizStatisticsUseCase {
        return GetQuizStatisticsUseCase(repository)
    }
}
