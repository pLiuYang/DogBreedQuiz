package com.dogbreedquiz.app.ui.screens.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme
import kotlin.collections.take

/**
 * Data class for progress statistics
 */
data class ProgressStats(
    val totalQuizzes: Int,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val accuracy: Int,
    val currentStreak: Int,
    val bestStreak: Int
)

/**
 * Data class for breed mastery tracking
 */
data class BreedMastery(
    val breedId: String,
    val breedName: String,
    val correctAnswers: Int,
    val totalAnswers: Int,
    val masteryLevel: MasteryLevel
) {
    enum class MasteryLevel {
        MASTER, EXPERT, PROFICIENT, LEARNING, NOVICE
    }
    
    val accuracy: Float = if (totalAnswers > 0) correctAnswers.toFloat() / totalAnswers else 0f
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Mock data - in real app, this would come from ViewModel
    val level = 12
    val experience = 1250
    val experienceToNext = 1500
    val progress = experience.toFloat() / experienceToNext
    
    val stats = ProgressStats(
        totalQuizzes = 47,
        correctAnswers = 342,
        totalAnswers = 394,
        accuracy = 87,
        currentStreak = 12,
        bestStreak = 28
    )
    
    val breedMasteries = getSampleBreedMasteries()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.your_progress),
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Level Progress Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DogBreedColors.OffWhite
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.level_format, level),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = DogBreedColors.PrimaryBlue,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Text(
                            text = "🏆",
                            fontSize = 32.sp
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp),
                            color = DogBreedColors.PrimaryBlue,
                            trackColor = DogBreedColors.LightGray,
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = stringResource(R.string.xp_format, experience, experienceToNext),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DogBreedColors.SecondaryText
                            )
                        )
                    }
                }
            }
            
            // Quiz Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DogBreedColors.BackgroundWhite
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.quiz_stats),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = DogBreedColors.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Stats Grid
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatRow(
                                label = "Total Quizzes",
                                value = stats.totalQuizzes.toString()
                            )
                            
                            StatRow(
                                label = "Correct Answers",
                                value = stats.correctAnswers.toString()
                            )
                            
                            StatRow(
                                label = "Accuracy",
                                value = "${stats.accuracy}%",
                                valueColor = when {
                                    stats.accuracy >= 90 -> DogBreedColors.SuccessGreen
                                    stats.accuracy >= 70 -> DogBreedColors.WarningOrange
                                    else -> DogBreedColors.ErrorRed
                                }
                            )
                            
                            StatRow(
                                label = "Current Streak",
                                value = "🔥 ${stats.currentStreak}",
                                valueColor = DogBreedColors.WarningOrange
                            )
                            
                            StatRow(
                                label = "Best Streak",
                                value = "🔥 ${stats.bestStreak}",
                                valueColor = DogBreedColors.WarningOrange
                            )
                        }
                    }
                }
            }
            
            // Breed Mastery Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = DogBreedColors.BackgroundWhite
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.breed_mastery),
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = DogBreedColors.DarkGray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Top Breed Masteries
                        breedMasteries.take(4).forEach { mastery ->
                            BreedMasteryRow(mastery = mastery)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(
                            onClick = { /* Navigate to full breed list */ },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.view_all_breeds),
                                color = DogBreedColors.PrimaryBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatRow(
    label: String,
    value: String,
    valueColor: Color = DogBreedColors.DarkGray
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DogBreedColors.SecondaryText
            )
        )
        
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = valueColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun BreedMasteryRow(
    mastery: BreedMastery
) {
    val masteryIcon = when (mastery.masteryLevel) {
        BreedMastery.MasteryLevel.MASTER -> "🥇"
        BreedMastery.MasteryLevel.EXPERT -> "🥈"
        BreedMastery.MasteryLevel.PROFICIENT -> "🥉"
        BreedMastery.MasteryLevel.LEARNING -> "📚"
        BreedMastery.MasteryLevel.NOVICE -> "🌱"
    }
    
    val masteryColor = when (mastery.masteryLevel) {
        BreedMastery.MasteryLevel.MASTER -> DogBreedColors.SuccessGreen
        BreedMastery.MasteryLevel.EXPERT -> DogBreedColors.PrimaryBlue
        BreedMastery.MasteryLevel.PROFICIENT -> DogBreedColors.WarningOrange
        BreedMastery.MasteryLevel.LEARNING -> DogBreedColors.SecondaryText
        BreedMastery.MasteryLevel.NOVICE -> DogBreedColors.LightGray
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = masteryIcon,
                fontSize = 20.sp
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = mastery.breedName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DogBreedColors.DarkGray
                )
            )
        }
        
        Text(
            text = "${(mastery.accuracy * 100).toInt()}%",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = masteryColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

private fun getSampleBreedMasteries(): List<BreedMastery> {
    return listOf(
        BreedMastery(
            breedId = "golden_retriever",
            breedName = "Golden Retriever",
            correctAnswers = 10,
            totalAnswers = 10,
            masteryLevel = BreedMastery.MasteryLevel.MASTER
        ),
        BreedMastery(
            breedId = "labrador",
            breedName = "Labrador",
            correctAnswers = 9,
            totalAnswers = 10,
            masteryLevel = BreedMastery.MasteryLevel.EXPERT
        ),
        BreedMastery(
            breedId = "german_shepherd",
            breedName = "German Shepherd",
            correctAnswers = 9,
            totalAnswers = 10,
            masteryLevel = BreedMastery.MasteryLevel.EXPERT
        ),
        BreedMastery(
            breedId = "beagle",
            breedName = "Beagle",
            correctAnswers = 7,
            totalAnswers = 8,
            masteryLevel = BreedMastery.MasteryLevel.PROFICIENT
        ),
        BreedMastery(
            breedId = "border_collie",
            breedName = "Border Collie",
            correctAnswers = 6,
            totalAnswers = 8,
            masteryLevel = BreedMastery.MasteryLevel.LEARNING
        )
    )
}

@Preview(showBackground = true)
@Composable
fun ProgressScreenPreview() {
    DogBreedQuizTheme {
        ProgressScreen()
    }
}