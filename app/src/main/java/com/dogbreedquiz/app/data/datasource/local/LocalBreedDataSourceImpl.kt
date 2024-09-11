package com.dogbreedquiz.app.data.datasource.local

import com.dogbreedquiz.app.data.database.dao.BreedDao
import com.dogbreedquiz.app.data.database.entity.BreedEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of LocalBreedDataSource using Room database
 * Handles all local storage operations for breed data
 */
@Singleton
class LocalBreedDataSourceImpl @Inject constructor(
    private val breedDao: BreedDao
) : LocalBreedDataSource {
    
    override suspend fun getAllBreeds(): List<BreedEntity> {
        return breedDao.getValidBreeds()
    }
    
    override fun getAllBreedsFlow(): Flow<List<BreedEntity>> {
        return breedDao.getValidBreedsFlow()
    }
    
    override suspend fun getBreedById(id: String): BreedEntity? {
        return breedDao.getBreedById(id)
    }
    
    override fun getBreedByIdFlow(id: String): Flow<BreedEntity?> {
        return breedDao.getBreedByIdFlow(id)
    }
    
    override suspend fun saveBreeds(breeds: List<BreedEntity>) {
        breedDao.insertBreeds(breeds)
    }
    
    override suspend fun saveBreed(breed: BreedEntity) {
        breedDao.insertBreeds(listOf(breed))
    }
    
    override suspend fun updateBreedImage(breedId: String, imageUrl: String) {
        breedDao.updateBreedImage(breedId, imageUrl)
    }
    
    override suspend fun getBreedsByDifficulty(difficulty: String): List<BreedEntity> {
        return breedDao.getBreedsByDifficulty(difficulty)
    }
    
    override suspend fun getBreedsBySize(size: String): List<BreedEntity> {
        return breedDao.getBreedsBySize(size)
    }
    
    override suspend fun searchBreeds(query: String): List<BreedEntity> {
        return breedDao.searchBreeds(query)
    }
    
    override suspend fun getFavoriteBreeds(): List<BreedEntity> {
        return breedDao.getFavoriteBreeds()
    }
    
    override fun getFavoriteBreedsFlow(): Flow<List<BreedEntity>> {
        return breedDao.getFavoriteBreedsFlow()
    }
    
    override suspend fun updateFavoriteStatus(breedId: String, isFavorite: Boolean) {
        breedDao.updateFavoriteStatus(breedId, isFavorite)
    }
    
    override suspend fun getRandomBreeds(count: Int): List<BreedEntity> {
        return breedDao.getRandomBreeds(count)
    }
    
    override suspend fun clearAllBreeds() {
        breedDao.deleteAllBreeds()
    }
    
    override suspend fun isDataValid(): Boolean {
        val validBreeds = breedDao.getValidBreeds()
        return validBreeds.isNotEmpty()
    }
}