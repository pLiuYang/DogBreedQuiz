package com.dogbreedquiz.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun HomeScreen(
    onStartQuiz: () -> Unit = {},
    onViewProgress: () -> Unit = {},
    onViewAchievements: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    // Mock user progress data
    val level = 12
    val experience = 1250
    val experienceToNext = 1500
    val progress = experience.toFloat() / experienceToNext
    val streak = 5

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with app title
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🐕 DOG QUIZ 🐕",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = DogBreedColors.PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.settings),
                    tint = DogBreedColors.MediumGray
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User Progress Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DogBreedColors.OffWhite
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.level_format, level),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Text(
                    text = "🏆",
                    fontSize = 24.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Main Action Button
        Button(
            onClick = onStartQuiz,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DogBreedColors.PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = stringResource(R.string.start_quiz),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Secondary Action Button
        OutlinedButton(
            onClick = { /* Navigate to breed learning */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = DogBreedColors.PrimaryBlue
            ),
            border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.learn_breeds),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Stats and Settings Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Stats Card
            Card(
                onClick = onViewProgress,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DogBreedColors.OffWhite
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = stringResource(R.string.stats),
                        tint = DogBreedColors.PrimaryBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.stats),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
            
            // Achievements Card
            Card(
                onClick = onViewAchievements,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DogBreedColors.OffWhite
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = stringResource(R.string.achievements),
                        tint = DogBreedColors.WarningOrange,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Badges",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Daily Challenge Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DogBreedColors.LightGreen
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.daily_challenge),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "🎯 \"Sporting Dogs\"",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DogBreedColors.DarkGray
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Streak: ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = DogBreedColors.WarningOrange,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " $streak",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.WarningOrange,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onStartQuiz,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DogBreedColors.SuccessGreen,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.take_challenge),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    DogBreedQuizTheme {
        HomeScreen()
    }
}