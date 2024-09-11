package com.dogbreedquiz.app.data.repository

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import com.dogbreedquiz.app.data.datasource.local.LocalBreedDataSource
import com.dogbreedquiz.app.data.datasource.remote.RemoteBreedDataSource
import com.dogbreedquiz.app.data.mapper.BreedMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of DogBreedRepository interface
 * Coordinates between local and remote data sources with proper caching strategy
 */
@Singleton
class DogBreedRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteBreedDataSource,
    private val localDataSource: LocalBreedDataSource,
    private val mapper: BreedMapper
) : DogBreedRepository {
    
    override suspend fun getAllBreeds(forceRefresh: Boolean): List<DogBreed> {
        return try {
            // Check cache first unless force refresh is requested
            if (!forceRefresh) {
                val cachedBreeds = localDataSource.getAllBreeds()
                if (cachedBreeds.isNotEmpty()) {
                    return cachedBreeds.map { mapper.mapToDomain(it) }
                }
            }
            
            // Fetch from remote source
            val remoteBreeds = remoteDataSource.getAllBreeds()
            
            // Cache the results
            localDataSource.saveBreeds(remoteBreeds)
            
            // Map to domain models
            remoteBreeds.map { mapper.mapToDomain(it) }
        } catch (e: Exception) {
            // Fallback to cached data if available
            val cachedBreeds = localDataSource.getAllBreeds()
            if (cachedBreeds.isNotEmpty()) {
                cachedBreeds.map { mapper.mapToDomain(it) }
            } else {
                // Return fallback static data
                getFallbackBreeds()
            }
        }
    }
    
    override fun getAllBreedsFlow(): Flow<List<DogBreed>> {
        return localDataSource.getAllBreedsFlow()
            .map { breeds -> breeds.map { mapper.mapToDomain(it) } }
    }
    
    override suspend fun getBreedById(id: String): DogBreed? {
        val breedEntity = localDataSource.getBreedById(id)
        return breedEntity?.let { mapper.mapToDomain(it) }
    }
    
    override fun getBreedByIdFlow(id: String): Flow<DogBreed?> {
        return localDataSource.getBreedByIdFlow(id)
            .map { it?.let { breed -> mapper.mapToDomain(breed) } }
    }
    
    override suspend fun getBreedsByDifficulty(difficulty: DogBreed.Difficulty): List<DogBreed> {
        val breeds = localDataSource.getBreedsByDifficulty(difficulty.name)
        return breeds.map { mapper.mapToDomain(it) }
    }
    
    override suspend fun getBreedsBySize(size: DogBreed.Size): List<DogBreed> {
        val breeds = localDataSource.getBreedsBySize(size.name)
        return breeds.map { mapper.mapToDomain(it) }
    }
    
    override suspend fun searchBreeds(query: String): List<DogBreed> {
        val breeds = localDataSource.searchBreeds(query)
        return breeds.map { mapper.mapToDomain(it) }
    }
    
    override suspend fun getFavoriteBreeds(): List<DogBreed> {
        val breeds = localDataSource.getFavoriteBreeds()
        return breeds.map { mapper.mapToDomain(it) }
    }
    
    override fun getFavoriteBreedsFlow(): Flow<List<DogBreed>> {
        return localDataSource.getFavoriteBreedsFlow()
            .map { breeds -> breeds.map { mapper.mapToDomain(it) } }
    }
    
    override suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean) {
        localDataSource.updateFavoriteStatus(breedId, isFavorite)
    }
    
    override suspend fun getRandomBreeds(count: Int): List<DogBreed> {
        val breeds = localDataSource.getRandomBreeds(count)
        return breeds.map { mapper.mapToDomain(it) }
    }
    
    override suspend fun loadBreedImage(breedId: String): DogBreed? {
        return try {
            // Get breed from local storage
            val breedEntity = localDataSource.getBreedById(breedId) ?: return null
            
            // Check if image is already cached
            if (breedEntity.imageUrl.isNotEmpty()) {
                return mapper.mapToDomain(breedEntity)
            }
            
            // Load image from remote source
            val imageUrl = remoteDataSource.getBreedImage(breedId)
            
            // Update local storage with image URL
            localDataSource.updateBreedImage(breedId, imageUrl)
            
            // Return updated breed
            val updatedBreed = localDataSource.getBreedById(breedId)
            updatedBreed?.let { mapper.mapToDomain(it) }
        } catch (e: Exception) {
            // Return breed without image if loading fails
            val breedEntity = localDataSource.getBreedById(breedId)
            breedEntity?.let { mapper.mapToDomain(it) }
        }
    }
    
    override suspend fun loadBreedImages(breedIds: List<String>): Map<String, String> {
        val results = mutableMapOf<String, String>()
        
        for (breedId in breedIds) {
            try {
                val breed = loadBreedImage(breedId)
                results[breedId] = breed?.imageUrl ?: ""
            } catch (e: Exception) {
                results[breedId] = ""
            }
        }
        
        return results
    }
    
    /**
     * Provide fallback breeds when all data sources fail
     */
    private fun getFallbackBreeds(): List<DogBreed> {
        return listOf(
            DogBreed(
                id = "golden_retriever",
                name = "Golden Retriever",
                imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop",
                description = "A friendly, intelligent, and devoted dog. Golden Retrievers are serious workers at hunting and field work, as guides for the blind, and in search-and-rescue.",
                funFact = "Golden Retrievers were originally bred in Scotland for hunting waterfowl!",
                origin = "Scotland",
                size = DogBreed.Size.LARGE,
                temperament = listOf("Friendly", "Intelligent", "Devoted"),
                lifeSpan = "10-12 years",
                difficulty = DogBreed.Difficulty.BEGINNER
            ),
            DogBreed(
                id = "labrador_retriever",
                name = "Labrador Retriever",
                imageUrl = "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&h=300&fit=crop",
                description = "Labs are friendly, outgoing, and active companions who have more than enough affection to go around for a family looking for a medium to large dog.",
                funFact = "Labradors are the most popular dog breed in the United States!",
                origin = "Newfoundland, Canada",
                size = DogBreed.Size.LARGE,
                temperament = listOf("Outgoing", "Even Tempered", "Gentle"),
                lifeSpan = "10-12 years",
                difficulty = DogBreed.Difficulty.BEGINNER
            )
        )
    }
}