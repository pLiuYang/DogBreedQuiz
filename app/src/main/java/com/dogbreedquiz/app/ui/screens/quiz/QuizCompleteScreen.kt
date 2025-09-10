package com.dogbreedquiz.app.ui.screens.quiz

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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

@Composable
fun QuizCompleteScreen(
    onRetry: () -> Unit = {},
    onMainMenu: () -> Unit = {},
    onShare: () -> Unit = {},
    // Mock data - in real app, this would come from ViewModel
    score: Int = 8,
    totalQuestions: Int = 10,
    accuracy: Int = 80,
    pointsEarned: Int = 800,
    correctAnswers: Int = 8,
    wrongAnswers: Int = 2,
    streak: Int = 5,
    timeSpent: String = "3:24",
    isNewLevel: Boolean = true,
    oldLevel: Int = 12,
    newLevel: Int = 13
) {
    // Celebration animations
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val confettiScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "confetti_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Celebration Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎉 ${stringResource(R.string.quiz_complete)} 🎉",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = DogBreedColors.SuccessGreen,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Animated Confetti
            Text(
                text = "🎊 ✨ 🎊 ✨ 🎊",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(confettiScale)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Score Display
            Text(
                text = stringResource(R.string.your_score, score, totalQuestions),
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Text(
                text = stringResource(R.string.accuracy_percent, accuracy),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DogBreedColors.PrimaryBlue,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Points Earned
            Text(
                text = stringResource(R.string.points_earned, pointsEarned),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DogBreedColors.SuccessGreen,
                    fontWeight = FontWeight.Bold
                )
            )
        }
        
        // Performance Card
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
                    text = stringResource(R.string.performance),
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Performance Stats
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PerformanceRow(
                        label = "Correct / Wrong",
                        value = stringResource(R.string.correct_wrong, correctAnswers, wrongAnswers)
                    )
                    
                    PerformanceRow(
                        label = "Streak",
                        value = "🔥 $streak"
                    )
                    
                    PerformanceRow(
                        label = "Time",
                        value = stringResource(R.string.time_taken, timeSpent)
                    )
                    
                    if (isNewLevel) {
                        PerformanceRow(
                            label = "Level Up!",
                            value = stringResource(R.string.new_level, oldLevel, newLevel),
                            valueColor = DogBreedColors.SuccessGreen,
                            isSpecial = true
                        )
                    }
                }
            }
        }
        
        // Level Up Celebration (if applicable)
        if (isNewLevel) {
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
                        text = "🎊 LEVEL UP! 🎊",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = DogBreedColors.SuccessGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.scale(scale)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "You've reached Level $newLevel!",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = DogBreedColors.DarkGray
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Keep learning to unlock more breeds and achievements!",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Share and Retry Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onShare,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = DogBreedColors.PrimaryBlue
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.share),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                
                Button(
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DogBreedColors.WarningOrange,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.retry),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            
            // Main Menu Button
            Button(
                onClick = onMainMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DogBreedColors.PrimaryBlue,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.main_menu),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun PerformanceRow(
    label: String,
    value: String,
    valueColor: Color = DogBreedColors.DarkGray,
    isSpecial: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DogBreedColors.SecondaryText,
                fontWeight = if (isSpecial) FontWeight.SemiBold else FontWeight.Normal
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

@Preview(showBackground = true)
@Composable
fun QuizCompleteScreenPreview() {
    DogBreedQuizTheme {
        QuizCompleteScreen()
    }
}

@Preview(showBackground = true)
@Composable
fun QuizCompleteScreenNoLevelUpPreview() {
    DogBreedQuizTheme {
        QuizCompleteScreen(
            isNewLevel = false
        )
    }
}