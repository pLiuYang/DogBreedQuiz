package com.dogbreedquiz.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class DogBreed(
    val id: String,
    val name: String,
    val imageUrl: String,
    val description: String,
    val funFact: String,
    val origin: String,
    val size: Size,
    val temperament: List<String>,
    val lifeSpan: String,
    val difficulty: Difficulty
) {
    enum class Size {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }
    
    enum class Difficulty {
        BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
    }
}

@Serializable
data class QuizQuestion(
    val id: String,
    val correctBreed: DogBreed,
    val options: List<DogBreed>,
    val imageUrl: String = correctBreed.imageUrl
) {
    init {
        require(options.contains(correctBreed)) { "Correct breed must be in options" }
        require(options.size == 4) { "Quiz must have exactly 4 options" }
    }
}

@Serializable
data class QuizSession(
    val id: String,
    val questions: List<QuizQuestion>,
    val currentQuestionIndex: Int = 0,
    val score: Int = 0,
    val correctAnswers: Int = 0,
    val startTime: Long = System.currentTimeMillis(),
    val answers: List<QuizAnswer> = emptyList()
) {
    val isComplete: Boolean get() = currentQuestionIndex >= questions.size
    val currentQuestion: QuizQuestion? get() = questions.getOrNull(currentQuestionIndex)
    val totalQuestions: Int get() = questions.size
    val accuracy: Float get() = if (answers.isEmpty()) 0f else correctAnswers.toFloat() / answers.size
}

@Serializable
data class QuizAnswer(
    val questionId: String,
    val selectedBreed: DogBreed,
    val correctBreed: DogBreed,
    val isCorrect: Boolean,
    val timeSpent: Long,
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class UserProgress(
    val level: Int = 1,
    val experience: Int = 0,
    val totalQuizzes: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val breedMastery: Map<String, BreedMastery> = emptyMap(),
    val achievements: List<Achievement> = emptyList(),
    val lastPlayedDate: Long = 0L
) {
    val accuracy: Float get() = if (totalAnswers == 0) 0f else totalCorrectAnswers.toFloat() / totalAnswers
    val experienceToNextLevel: Int get() = (level * 100) - (experience % (level * 100))
    val experienceProgress: Float get() = (experience % (level * 100)).toFloat() / (level * 100)
}

@Serializable
data class BreedMastery(
    val breedId: String,
    val breedName: String,
    val correctAnswers: Int = 0,
    val totalAnswers: Int = 0,
    val masteryLevel: MasteryLevel = MasteryLevel.NOVICE
) {
    val accuracy: Float get() = if (totalAnswers == 0) 0f else correctAnswers.toFloat() / totalAnswers
    
    enum class MasteryLevel {
        NOVICE,     // 0-49% accuracy
        LEARNING,   // 50-74% accuracy
        PROFICIENT, // 75-89% accuracy
        EXPERT,     // 90-99% accuracy
        MASTER      // 100% accuracy with 10+ attempts
    }
}

@Serializable
data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val unlockedAt: Long,
    val category: AchievementCategory
) {
    enum class AchievementCategory {
        LEARNING, PERFORMANCE, ENGAGEMENT, SOCIAL
    }
}

@Serializable
data class GameSettings(
    val difficultyLevel: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
    val questionTimer: Int = 30, // seconds, 0 for no timer
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val dailyRemindersEnabled: Boolean = true,
    val reminderTime: String = "18:00", // 24-hour format
    val achievementAlertsEnabled: Boolean = true
)