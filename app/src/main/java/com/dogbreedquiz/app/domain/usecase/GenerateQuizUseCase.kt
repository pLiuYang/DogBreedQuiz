package com.dogbreedquiz.app.domain.usecase

import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.QuizQuestion
import com.dogbreedquiz.app.domain.model.QuizSession
import com.dogbreedquiz.app.domain.repository.DogBreedRepository
import javax.inject.Inject
import kotlin.random.Random

/**
 * Use case for generating quiz sessions
 * Encapsulates the complex business logic for creating balanced quizzes
 */
class GenerateQuizUseCase @Inject constructor(
    private val breedRepository: DogBreedRepository
) {
    
    /**
     * Generate a new quiz session with specified parameters
     * @param difficulty Target difficulty level
     * @param questionCount Number of questions to generate
     * @return Generated quiz session
     */
    suspend operator fun invoke(
        difficulty: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
        questionCount: Int = 10
    ): Result<QuizSession> {
        return try {
            val availableBreeds = getAvailableBreedsForDifficulty(difficulty)
            
            if (availableBreeds.size < 4) {
                return Result.failure(
                    IllegalStateException("Not enough breeds available for quiz generation")
                )
            }
            
            val questions = generateQuestions(availableBreeds, questionCount)
            val session = QuizSession(
                id = generateSessionId(),
                questions = questions,
                difficulty = difficulty
            )
            
            Result.success(session)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get breeds suitable for the specified difficulty level
     */
    private suspend fun getAvailableBreedsForDifficulty(difficulty: DogBreed.Difficulty): List<DogBreed> {
        val allBreeds = breedRepository.getAllBreeds()
        
        return when (difficulty) {
            DogBreed.Difficulty.BEGINNER -> {
                // For beginners, include easy and some intermediate breeds
                allBreeds.filter { 
                    it.difficulty in listOf(DogBreed.Difficulty.BEGINNER, DogBreed.Difficulty.INTERMEDIATE)
                }
            }
            DogBreed.Difficulty.INTERMEDIATE -> {
                // For intermediate, include beginner, intermediate, and some advanced
                allBreeds.filter { 
                    it.difficulty in listOf(
                        DogBreed.Difficulty.BEGINNER, 
                        DogBreed.Difficulty.INTERMEDIATE, 
                        DogBreed.Difficulty.ADVANCED
                    )
                }
            }
            DogBreed.Difficulty.ADVANCED -> {
                // For advanced, include intermediate, advanced, and expert
                allBreeds.filter { 
                    it.difficulty in listOf(
                        DogBreed.Difficulty.INTERMEDIATE, 
                        DogBreed.Difficulty.ADVANCED, 
                        DogBreed.Difficulty.EXPERT
                    )
                }
            }
            DogBreed.Difficulty.EXPERT -> {
                // For experts, include all breeds with emphasis on harder ones
                allBreeds
            }
        }
    }
    
    /**
     * Generate individual quiz questions
     */
    private fun generateQuestions(
        availableBreeds: List<DogBreed>, 
        questionCount: Int
    ): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val usedBreeds = mutableSetOf<String>()
        
        repeat(questionCount.coerceAtMost(availableBreeds.size)) {
            val correctBreed = selectCorrectBreed(availableBreeds, usedBreeds)
            usedBreeds.add(correctBreed.id)
            
            val incorrectOptions = selectIncorrectOptions(availableBreeds, correctBreed, 3)
            val allOptions = (incorrectOptions + correctBreed).shuffled()
            
            questions.add(
                QuizQuestion(
                    id = generateQuestionId(questions.size + 1),
                    correctBreed = correctBreed,
                    options = allOptions,
                    imageUrl = correctBreed.imageUrl
                )
            )
        }
        
        return questions
    }
    
    /**
     * Select the correct breed for a question, avoiding duplicates when possible
     */
    private fun selectCorrectBreed(
        availableBreeds: List<DogBreed>, 
        usedBreeds: Set<String>
    ): DogBreed {
        val unusedBreeds = availableBreeds.filter { it.id !in usedBreeds }
        return if (unusedBreeds.isNotEmpty()) {
            unusedBreeds.random()
        } else {
            availableBreeds.random()
        }
    }
    
    /**
     * Select incorrect options for a question, ensuring variety
     */
    private fun selectIncorrectOptions(
        availableBreeds: List<DogBreed>, 
        correctBreed: DogBreed, 
        count: Int
    ): List<DogBreed> {
        val incorrectBreeds = availableBreeds.filter { it.id != correctBreed.id }
        
        return if (incorrectBreeds.size >= count) {
            // Try to select breeds with similar characteristics for better difficulty balance
            val similarBreeds = incorrectBreeds.filter { 
                it.size == correctBreed.size || it.difficulty == correctBreed.difficulty 
            }
            
            val selected = mutableListOf<DogBreed>()
            
            // Add some similar breeds for challenge
            if (similarBreeds.isNotEmpty()) {
                selected.addAll(similarBreeds.shuffled().take(count / 2))
            }
            
            // Fill remaining with random breeds
            val remaining = count - selected.size
            if (remaining > 0) {
                val otherBreeds = incorrectBreeds.filter { it !in selected }
                selected.addAll(otherBreeds.shuffled().take(remaining))
            }
            
            selected.take(count)
        } else {
            incorrectBreeds.shuffled().take(count)
        }
    }
    
    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        return "quiz_session_${System.currentTimeMillis()}_${Random.nextInt(1000, 9999)}"
    }
    
    /**
     * Generate unique question ID
     */
    private fun generateQuestionId(questionNumber: Int): String {
        return "question_${questionNumber}_${Random.nextInt(100, 999)}"
    }
}