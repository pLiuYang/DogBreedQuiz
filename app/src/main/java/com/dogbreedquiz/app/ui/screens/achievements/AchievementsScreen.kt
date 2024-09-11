package com.dogbreedquiz.app.ui.screens.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Mock data - in real app, this would come from ViewModel
    val recentAchievements = getSampleRecentAchievements()
    val earnedAchievements = getSampleEarnedAchievements()
    val inProgressAchievements = getSampleInProgressAchievements()

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.achievements),
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
            // Recent Achievements
            if (recentAchievements.isNotEmpty()) {
                item {
                    AchievementSection(
                        title = stringResource(R.string.recent),
                        achievements = recentAchievements,
                        isRecent = true
                    )
                }
            }
            
            // Earned Achievements
            item {
                AchievementSection(
                    title = stringResource(R.string.earned),
                    achievements = earnedAchievements,
                    isEarned = true
                )
            }
            
            // In Progress Achievements
            item {
                AchievementSection(
                    title = stringResource(R.string.in_progress),
                    achievements = inProgressAchievements,
                    isInProgress = true
                )
            }
        }
    }
}

@Composable
private fun AchievementSection(
    title: String,
    achievements: List<AchievementDisplay>,
    isRecent: Boolean = false,
    isEarned: Boolean = false,
    isInProgress: Boolean = false
) {
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
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            when {
                isRecent -> {
                    achievements.forEach { achievement ->
                        RecentAchievementItem(achievement = achievement)
                        if (achievement != achievements.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                
                isEarned -> {
                    // Grid layout for earned achievements
                    val chunkedAchievements = achievements.chunked(2)
                    chunkedAchievements.forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { achievement ->
                                EarnedAchievementItem(
                                    achievement = achievement,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if odd number
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (row != chunkedAchievements.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
                
                isInProgress -> {
                    achievements.forEach { achievement ->
                        InProgressAchievementItem(achievement = achievement)
                        if (achievement != achievements.last()) {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentAchievementItem(
    achievement: AchievementDisplay
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DogBreedColors.LightGreen
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = achievement.icon,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DogBreedColors.SecondaryText
                    )
                )
                
                achievement.earnedDate?.let { date ->
                    Text(
                        text = "Earned $date",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.SuccessGreen,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun EarnedAchievementItem(
    achievement: AchievementDisplay,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DogBreedColors.OffWhite
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = achievement.icon,
                fontSize = 32.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun InProgressAchievementItem(
    achievement: AchievementDisplay
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = DogBreedColors.OffWhite
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (achievement.isLocked) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Locked",
                        tint = DogBreedColors.MediumGray,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Text(
                        text = achievement.icon,
                        fontSize = 32.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = achievement.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = if (achievement.isLocked) DogBreedColors.MediumGray else DogBreedColors.DarkGray,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        )
                    )
                    
                    achievement.progress?.let { progress ->
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Progress: ${progress.current}/${progress.total}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DogBreedColors.PrimaryBlue,
                                fontWeight = FontWeight.Medium
                            )
                        )
                        
                        LinearProgressIndicator(
                            progress = { progress.current.toFloat() / progress.total },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = DogBreedColors.PrimaryBlue,
                            trackColor = DogBreedColors.LightGray,
                        )
                        
                        Text(
                            text = "${((progress.current.toFloat() / progress.total) * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DogBreedColors.SecondaryText
                            )
                        )
                    }
                }
            }
        }
    }
}

data class AchievementDisplay(
    val title: String,
    val description: String,
    val icon: String,
    val isLocked: Boolean = false,
    val earnedDate: String? = null,
    val progress: AchievementProgress? = null
)

data class AchievementProgress(
    val current: Int,
    val total: Int
)

private fun getSampleRecentAchievements(): List<AchievementDisplay> {
    return listOf(
        AchievementDisplay(
            title = "Quiz Master",
            description = "Complete 25 quizzes",
            icon = "🏆",
            earnedDate = "2 days ago"
        )
    )
}

private fun getSampleEarnedAchievements(): List<AchievementDisplay> {
    return listOf(
        AchievementDisplay(
            title = "First Quiz",
            description = "Complete your first quiz",
            icon = "🥇"
        ),
        AchievementDisplay(
            title = "Streak",
            description = "Get 5 correct in a row",
            icon = "🔥"
        ),
        AchievementDisplay(
            title = "Sharp Eye",
            description = "Identify 10 breeds correctly",
            icon = "🎯"
        ),
        AchievementDisplay(
            title = "Scholar",
            description = "Read 20 breed facts",
            icon = "📚"
        ),
        AchievementDisplay(
            title = "Rising Star",
            description = "Reach level 5",
            icon = "⭐"
        ),
        AchievementDisplay(
            title = "Master",
            description = "Reach level 10",
            icon = "🏆"
        )
    )
}

private fun getSampleInProgressAchievements(): List<AchievementDisplay> {
    return listOf(
        AchievementDisplay(
            title = "Perfectionist",
            description = "Get 50 correct answers in a row",
            icon = "💎",
            progress = AchievementProgress(12, 50)
        ),
        AchievementDisplay(
            title = "Breed Expert",
            description = "Master 20 different breeds",
            icon = "🎓",
            progress = AchievementProgress(8, 20)
        ),
        AchievementDisplay(
            title = "Speed Demon",
            description = "Answer 100 questions in under 3 seconds",
            icon = "⚡",
            progress = AchievementProgress(23, 100)
        )
    )
}

@Preview(showBackground = true)
@Composable
fun AchievementsScreenPreview() {
    DogBreedQuizTheme {
        AchievementsScreen()
    }
}