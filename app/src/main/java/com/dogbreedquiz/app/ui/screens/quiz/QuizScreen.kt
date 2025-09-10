package com.dogbreedquiz.app.ui.screens.quiz

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.data.model.DogBreed
import com.dogbreedquiz.app.data.model.QuizQuestion
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    onAnswerCorrect: (String) -> Unit = {},
    onAnswerIncorrect: (String) -> Unit = {},
    onQuizComplete: () -> Unit = {},
    onNavigateBack: () -> Unit = {},
    viewModel: QuizViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.startNewQuiz()
    }
    
    LaunchedEffect(uiState.isQuizComplete) {
        if (uiState.isQuizComplete) {
            onQuizComplete()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar with Stats
        TopAppBar(
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Streak
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = DogBreedColors.WarningOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = " ${uiState.currentStreak}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    // Score
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Score",
                            tint = DogBreedColors.PrimaryBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = " ${uiState.score}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    
                    // Lives
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = "Lives",
                            tint = DogBreedColors.ErrorRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = " ${uiState.lives}/3",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
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
        
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = DogBreedColors.PrimaryBlue
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading dog breeds...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (uiState.error != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Oops! Something went wrong",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = uiState.error ?: "Unknown error occurred",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = DogBreedColors.SecondaryText
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.startNewQuiz() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DogBreedColors.PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Try Again")
                    }
                }
            }
        } else {
            uiState.currentQuestion?.let { question ->
                QuizContent(
                    question = question,
                    currentQuestionNumber = uiState.currentQuestionIndex + 1,
                    totalQuestions = uiState.totalQuestions,
                    selectedAnswer = uiState.selectedAnswer,
                    showResult = uiState.showResult,
                    onAnswerSelected = { breed ->
                        viewModel.selectAnswer(breed)
                    },
                    onContinue = {
                        if (uiState.isCorrect) {
                            onAnswerCorrect(question.id)
                        } else {
                            onAnswerIncorrect(question.id)
                        }
                        viewModel.nextQuestion()
                    }
                )
            }
        }
    }
}

@Composable
private fun QuizContent(
    question: QuizQuestion,
    currentQuestionNumber: Int,
    totalQuestions: Int,
    selectedAnswer: DogBreed?,
    showResult: Boolean,
    onAnswerSelected: (DogBreed) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress Indicator
        LinearProgressIndicator(
            progress = { currentQuestionNumber.toFloat() / totalQuestions },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = DogBreedColors.PrimaryBlue,
            trackColor = DogBreedColors.LightGray,
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Question Counter
        Text(
            text = stringResource(R.string.question_format, currentQuestionNumber, totalQuestions),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = DogBreedColors.SecondaryText
            ),
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Dog Image
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(question.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.dog_image_description),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Answer Options
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            question.options.forEach { breed ->
                AnswerOptionCard(
                    breed = breed,
                    isSelected = selectedAnswer == breed,
                    isCorrect = showResult && breed == question.correctBreed,
                    isIncorrect = showResult && selectedAnswer == breed && breed != question.correctBreed,
                    enabled = !showResult,
                    onClick = { onAnswerSelected(breed) }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Continue Button (shown after answer selection)
        if (showResult) {
            Button(
                onClick = onContinue,
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
                    text = stringResource(R.string.continue_button),
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
private fun AnswerOptionCard(
    breed: DogBreed,
    isSelected: Boolean,
    isCorrect: Boolean,
    isIncorrect: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCorrect -> DogBreedColors.LightGreen
            isIncorrect -> DogBreedColors.LightRed
            isSelected -> DogBreedColors.LightBlue
            else -> DogBreedColors.OffWhite
        },
        animationSpec = tween(300),
        label = "background_color"
    )
    
    val borderColor by animateColorAsState(
        targetValue = when {
            isCorrect -> DogBreedColors.BorderCorrect
            isIncorrect -> DogBreedColors.BorderIncorrect
            isSelected -> DogBreedColors.BorderSelected
            else -> DogBreedColors.BorderDefault
        },
        animationSpec = tween(300),
        label = "border_color"
    )

    Card(
        onClick = if (enabled) onClick else { {} },
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        enabled = enabled
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = breed.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color = when {
                            isCorrect -> DogBreedColors.SuccessText
                            isIncorrect -> DogBreedColors.ErrorText
                            isSelected -> DogBreedColors.PrimaryBlue
                            else -> DogBreedColors.DarkGray
                        }
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                if (isCorrect || (isSelected && !isIncorrect)) {
                    Text(
                        text = if (isCorrect) "✓" else "✓",
                        fontSize = 20.sp,
                        color = if (isCorrect) DogBreedColors.SuccessGreen else DogBreedColors.PrimaryBlue,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                } else if (isIncorrect) {
                    Text(
                        text = "✗",
                        fontSize = 20.sp,
                        color = DogBreedColors.ErrorRed,
                        modifier = Modifier.padding(end = 16.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QuizScreenPreview() {
    DogBreedQuizTheme {
        QuizScreen()
    }
}