package com.dogbreedquiz.app.data.mapper

import com.dogbreedquiz.app.data.database.entity.BreedEntity
import com.dogbreedquiz.app.domain.model.DogBreed
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mapper interface for converting between data and domain models
 * Provides clean separation between data layer entities and domain models
 */
interface BreedMapper {
    
    /**
     * Convert BreedEntity to DogBreed domain model
     */
    fun mapToDomain(entity: BreedEntity): DogBreed
    
    /**
     * Convert DogBreed domain model to BreedEntity
     */
    fun mapToEntity(domain: DogBreed): BreedEntity
    
    /**
     * Convert list of BreedEntity to list of DogBreed
     */
    fun mapToDomainList(entities: List<BreedEntity>): List<DogBreed>
    
    /**
     * Convert list of DogBreed to list of BreedEntity
     */
    fun mapToEntityList(domains: List<DogBreed>): List<BreedEntity>
}

/**
 * Implementation of BreedMapper
 */
@Singleton
class BreedMapperImpl @Inject constructor() : BreedMapper {
    
    override fun mapToDomain(entity: BreedEntity): DogBreed {
        return entity.toDogBreed()
    }
    
    override fun mapToEntity(domain: DogBreed): BreedEntity {
        return BreedEntity(
            id = domain.id,
            name = domain.name,
            imageUrl = domain.imageUrl,
            description = domain.description,
            funFact = domain.funFact,
            origin = domain.origin,
            size = domain.size.name,
            temperament = domain.temperament.joinToString(","),
            lifeSpan = domain.lifeSpan,
            difficulty = domain.difficulty.name,
            isFavorite = domain.isFavorite,
            cachedAt = System.currentTimeMillis(),
            expiresAt = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L), // 7 days
            lastUpdated = System.currentTimeMillis()
        )
    }
    
    override fun mapToDomainList(entities: List<BreedEntity>): List<DogBreed> {
        return entities.map { mapToDomain(it) }
    }
    
    override fun mapToEntityList(domains: List<DogBreed>): List<BreedEntity> {
        return domains.map { mapToEntity(it) }
    }
}