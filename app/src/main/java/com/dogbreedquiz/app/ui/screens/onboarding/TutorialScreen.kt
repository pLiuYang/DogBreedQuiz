package com.dogbreedquiz.app.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
fun TutorialScreen(
    step: Int = 1,
    onNext: (Int) -> Unit = {},
    onBack: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    val totalSteps = 3
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.how_to_play),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "$step/$totalSteps",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        )
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = stringResource(R.string.back)
                    )
                }
            },
            actions = {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        color = DogBreedColors.PrimaryBlue
                    )
                }
            }
        )
        
        // Progress Indicator
        LinearProgressIndicator(
            progress = { step.toFloat() / totalSteps },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            color = DogBreedColors.PrimaryBlue,
            trackColor = DogBreedColors.LightGray,
        )
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            when (step) {
                1 -> TutorialStep1()
                2 -> TutorialStep2()
                3 -> TutorialStep3()
            }
            
            // Next Button
            Button(
                onClick = { onNext(step + 1) },
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
                    text = if (step < totalSteps) stringResource(R.string.next) else "Start Quiz!",
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
private fun TutorialStep1() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Dog Photo Illustration
        Card(
            modifier = Modifier
                .size(200.dp, 150.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DogBreedColors.OffWhite
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🐕‍🦺",
                    fontSize = 64.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.tutorial_step_1),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DogBreedColors.DarkGray,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "We'll show you a photo of a dog breed. Take a good look at the features, size, and characteristics.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DogBreedColors.SecondaryText
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TutorialStep2() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Multiple Choice Illustration
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(4) { index ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (index == 1) DogBreedColors.LightBlue else DogBreedColors.OffWhite
                    ),
                    border = if (index == 1) CardDefaults.outlinedCardBorder().copy(
                        width = 2.dp
                    ) else null
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (index) {
                                0 -> "Golden Retriever"
                                1 -> "Labrador Retriever ✓"
                                2 -> "German Shepherd"
                                3 -> "Border Collie"
                                else -> ""
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (index == 1) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (index == 1) DogBreedColors.PrimaryBlue else DogBreedColors.DarkGray
                            )
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.tutorial_step_2),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DogBreedColors.DarkGray,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Select the breed name that matches the photo. Don't worry if you're not sure - you'll learn as you play!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DogBreedColors.SecondaryText
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TutorialStep3() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Success Illustration
        Card(
            modifier = Modifier.size(120.dp),
            shape = RoundedCornerShape(60.dp),
            colors = CardDefaults.cardColors(
                containerColor = DogBreedColors.LightGreen
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Success",
                    modifier = Modifier.size(48.dp),
                    tint = DogBreedColors.SuccessGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "⭐ ⭐ ⭐ ⭐ ⭐",
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = stringResource(R.string.tutorial_step_3),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = DogBreedColors.DarkGray,
                fontWeight = FontWeight.SemiBold
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Earn points, unlock achievements, and learn fascinating facts about dog breeds. Every answer helps you become a dog expert!",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = DogBreedColors.SecondaryText
            ),
            textAlign = TextAlign.Center
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialScreenPreview() {
    DogBreedQuizTheme {
        TutorialScreen(step = 1)
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialStep2Preview() {
    DogBreedQuizTheme {
        TutorialScreen(step = 2)
    }
}

@Preview(showBackground = true)
@Composable
fun TutorialStep3Preview() {
    DogBreedQuizTheme {
        TutorialScreen(step = 3)
    }
}