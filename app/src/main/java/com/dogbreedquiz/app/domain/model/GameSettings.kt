package com.dogbreedquiz.app.domain.model

/**
 * Data class for game settings configuration
 */
data class GameSettings(
    val difficultyLevel: DogBreed.Difficulty = DogBreed.Difficulty.BEGINNER,
    val questionTimer: Int = 30,
    val soundEffectsEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val dailyRemindersEnabled: Boolean = true,
    val reminderTime: String = "18:00",
    val achievementAlertsEnabled: Boolean = true
)