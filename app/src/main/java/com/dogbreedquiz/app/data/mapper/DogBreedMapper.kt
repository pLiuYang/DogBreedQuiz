package com.dogbreedquiz.app.data.mapper

import com.dogbreedquiz.app.data.api.model.ApiBreed
import com.dogbreedquiz.app.data.model.DogBreed
import kotlin.random.Random

/**
 * Mapper to convert API breed data to internal DogBreed model
 */
object DogBreedMapper {
    
    /**
     * Convert ApiBreed to DogBreed with generated metadata
     */
    fun mapApiBreedToDogBreed(
        apiBreed: ApiBreed,
        subBreed: String? = null,
        imageUrl: String
    ): DogBreed {
        val breedName = apiBreed.getDisplayName(subBreed)
        val breedId = apiBreed.getUniqueId(subBreed)
        
        return DogBreed(
            id = breedId,
            name = breedName,
            imageUrl = imageUrl,
            description = generateDescription(breedName),
            funFact = generateFunFact(breedName),
            origin = generateOrigin(breedName),
            size = generateSize(breedName),
            temperament = generateTemperament(breedName),
            lifeSpan = generateLifeSpan(),
            difficulty = generateDifficulty(breedName, subBreed != null)
        )
    }
    
    /**
     * Convert multiple ApiBreeds to DogBreeds with their sub-breeds
     */
    fun mapApiBreedsToDogBreeds(
        apiBreeds: List<ApiBreed>,
        imageUrlProvider: suspend (String, String?) -> String?
    ): suspend () -> List<DogBreed> = {
        val dogBreeds = mutableListOf<DogBreed>()
        
        for (apiBreed in apiBreeds) {
            // Add main breed if it has no sub-breeds or if we want to include it anyway
            if (apiBreed.subBreeds.isEmpty()) {
                val imageUrl = imageUrlProvider(apiBreed.name, null) ?: getDefaultImageUrl()
                dogBreeds.add(mapApiBreedToDogBreed(apiBreed, null, imageUrl))
            } else {
                // Add sub-breeds
                for (subBreed in apiBreed.subBreeds) {
                    val imageUrl = imageUrlProvider(apiBreed.name, subBreed) ?: getDefaultImageUrl()
                    dogBreeds.add(mapApiBreedToDogBreed(apiBreed, subBreed, imageUrl))
                }
            }
        }
        
        dogBreeds
    }
    
    private fun generateDescription(breedName: String): String {
        val descriptions = mapOf(
            "Golden Retriever" to "A friendly, intelligent, and devoted dog. Golden Retrievers are serious workers at hunting and field work, as guides for the blind, and in search-and-rescue.",
            "Labrador Retriever" to "Labs are friendly, outgoing, and active companions who have more than enough affection to go around for a family looking for a medium to large dog.",
            "German Shepherd" to "Large, athletic dogs, they are extremely versatile, serving as family companions, guard dogs, and in military service.",
            "Border Collie" to "A remarkable dog breed with unlimited energy, stamina, and working drive, all of which make them a premier herding dog.",
            "Beagle" to "Small, compact, and hardy, Beagles are active companions for kids and adults alike.",
            "Bulldog" to "Bulldogs are gentle, patient, and loving family companions, though they can be protective of their families.",
            "Poodle" to "Poodles are exceptional jumpers, so pet parents should ensure their yards are fenced. They make excellent watchdogs.",
            "Rottweiler" to "Rottweilers are large, powerful dogs and require extensive socialization and training from early puppyhood.",
            "Yorkshire Terrier" to "Yorkies are affectionate, but they also want lots of attention; the breed can be needy at times.",
            "Dachshund" to "The famously long, low silhouette, ever-alert expression, and bold, confident manner of the Dachshund have made him a superstar of the canine kingdom.",
            "Siberian Husky" to "Siberian Huskies are pack dogs, and they need an owner who is the clear leader of the pack.",
            "Shih Tzu" to "The Shih Tzu is a lively, alert toy dog with a long flowing double coat."
        )
        
        return descriptions[breedName] ?: generateGenericDescription(breedName)
    }
    
    private fun generateGenericDescription(breedName: String): String {
        val templates = listOf(
            "The $breedName is a wonderful companion dog known for its unique characteristics and loyal nature.",
            "A distinctive breed, the $breedName has been cherished by dog lovers for generations.",
            "$breedName dogs are known for their distinctive appearance and friendly temperament.",
            "The $breedName is a remarkable breed with a rich history and distinctive traits."
        )
        return templates.random()
    }
    
    private fun generateFunFact(breedName: String): String {
        val funFacts = mapOf(
            "Golden Retriever" to "Golden Retrievers were originally bred in Scotland for hunting waterfowl!",
            "Labrador Retriever" to "Labradors are the most popular dog breed in the United States!",
            "German Shepherd" to "German Shepherds are the second most popular dog breed and are known for their courage and versatility!",
            "Border Collie" to "Border Collies are considered the most intelligent dog breed and can learn over 1,000 words!",
            "Beagle" to "Beagles have about 220 million scent receptors, compared to humans who have only 5 million!",
            "Bulldog" to "Despite their tough appearance, Bulldogs are actually very gentle and make excellent family pets!",
            "Poodle" to "Poodles were originally bred as water retrievers and their distinctive haircut was designed to help them swim!",
            "Rottweiler" to "Rottweilers were originally bred to drive cattle to market and later used to pull carts for butchers!",
            "Yorkshire Terrier" to "Yorkshire Terriers were originally bred to catch rats in textile mills during the Industrial Revolution!",
            "Dachshund" to "Dachshunds were bred to hunt badgers in their dens, which explains their long, low bodies!",
            "Siberian Husky" to "Siberian Huskies can run up to 100 miles a day and were bred by the Chukchi people of Siberia!",
            "Shih Tzu" to "Shih Tzus were bred by Chinese royalty and their name means 'lion dog' in Chinese!"
        )
        
        return funFacts[breedName] ?: "The $breedName has many fascinating traits that make it a unique and special breed!"
    }
    
    private fun generateOrigin(breedName: String): String {
        val origins = mapOf(
            "Golden Retriever" to "Scotland",
            "Labrador Retriever" to "Newfoundland, Canada",
            "German Shepherd" to "Germany",
            "Border Collie" to "Scotland/England Border",
            "Beagle" to "England",
            "Bulldog" to "England",
            "Poodle" to "Germany/France",
            "Rottweiler" to "Germany",
            "Yorkshire Terrier" to "England",
            "Dachshund" to "Germany",
            "Siberian Husky" to "Siberia",
            "Shih Tzu" to "Tibet/China"
        )
        
        return origins[breedName] ?: when {
            breedName.contains("Shepherd") -> "Germany"
            breedName.contains("Terrier") -> "England"
            breedName.contains("Spaniel") -> "Spain"
            breedName.contains("Retriever") -> "Scotland"
            breedName.contains("Hound") -> "England"
            else -> "Various regions"
        }
    }
    
    private fun generateSize(breedName: String): DogBreed.Size {
        return when {
            breedName.contains("Great") || breedName.contains("Mastiff") || breedName.contains("Saint") -> DogBreed.Size.EXTRA_LARGE
            breedName.contains("Retriever") || breedName.contains("Shepherd") || breedName.contains("Rottweiler") || 
            breedName.contains("Husky") || breedName.contains("Doberman") -> DogBreed.Size.LARGE
            breedName.contains("Terrier") && !breedName.contains("Yorkshire") || breedName.contains("Collie") || 
            breedName.contains("Beagle") || breedName.contains("Bulldog") -> DogBreed.Size.MEDIUM
            breedName.contains("Yorkshire") || breedName.contains("Chihuahua") || breedName.contains("Pomeranian") || 
            breedName.contains("Shih Tzu") || breedName.contains("Dachshund") -> DogBreed.Size.SMALL
            else -> DogBreed.Size.MEDIUM
        }
    }
    
    private fun generateTemperament(breedName: String): List<String> {
        val temperaments = mapOf(
            "Golden Retriever" to listOf("Friendly", "Intelligent", "Devoted"),
            "Labrador Retriever" to listOf("Outgoing", "Even Tempered", "Gentle"),
            "German Shepherd" to listOf("Confident", "Courageous", "Smart"),
            "Border Collie" to listOf("Smart", "Work-Oriented", "Energetic"),
            "Beagle" to listOf("Amiable", "Determined", "Excitable"),
            "Bulldog" to listOf("Docile", "Willful", "Friendly"),
            "Poodle" to listOf("Active", "Alert", "Intelligent"),
            "Rottweiler" to listOf("Loyal", "Loving", "Confident Guardian"),
            "Yorkshire Terrier" to listOf("Affectionate", "Sprightly", "Tomboyish"),
            "Dachshund" to listOf("Curious", "Friendly", "Spunky"),
            "Siberian Husky" to listOf("Outgoing", "Mischievous", "Loyal"),
            "Shih Tzu" to listOf("Affectionate", "Playful", "Outgoing")
        )
        
        return temperaments[breedName] ?: generateGenericTemperament(breedName)
    }
    
    private fun generateGenericTemperament(breedName: String): List<String> {
        val allTraits = listOf(
            "Friendly", "Loyal", "Intelligent", "Active", "Gentle", "Playful",
            "Alert", "Confident", "Energetic", "Affectionate", "Brave", "Calm",
            "Independent", "Social", "Protective", "Curious", "Patient", "Lively"
        )
        return allTraits.shuffled().take(3)
    }
    
    private fun generateLifeSpan(): String {
        val lifespans = listOf(
            "8-10 years", "9-11 years", "10-12 years", "11-13 years", 
            "12-14 years", "12-15 years", "13-15 years", "14-16 years"
        )
        return lifespans.random()
    }
    
    private fun generateDifficulty(breedName: String, isSubBreed: Boolean): DogBreed.Difficulty {
        // Sub-breeds are generally more specific and harder to identify
        val baseDifficulty = when {
            breedName.contains("Retriever") || breedName.contains("Beagle") || 
            breedName.contains("Bulldog") || breedName.contains("Shih Tzu") -> DogBreed.Difficulty.BEGINNER
            
            breedName.contains("Shepherd") || breedName.contains("Collie") || 
            breedName.contains("Poodle") || breedName.contains("Terrier") -> DogBreed.Difficulty.INTERMEDIATE
            
            breedName.contains("Rottweiler") || breedName.contains("Husky") || 
            breedName.contains("Mastiff") -> DogBreed.Difficulty.ADVANCED
            
            else -> DogBreed.Difficulty.INTERMEDIATE
        }
        
        // Increase difficulty for sub-breeds
        return if (isSubBreed) {
            when (baseDifficulty) {
                DogBreed.Difficulty.BEGINNER -> DogBreed.Difficulty.INTERMEDIATE
                DogBreed.Difficulty.INTERMEDIATE -> DogBreed.Difficulty.ADVANCED
                DogBreed.Difficulty.ADVANCED -> DogBreed.Difficulty.EXPERT
                DogBreed.Difficulty.EXPERT -> DogBreed.Difficulty.EXPERT
            }
        } else {
            baseDifficulty
        }
    }
    
    private fun getDefaultImageUrl(): String {
        return "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop"
    }
}