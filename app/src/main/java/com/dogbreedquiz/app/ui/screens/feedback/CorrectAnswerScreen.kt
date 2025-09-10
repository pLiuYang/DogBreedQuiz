package com.dogbreedquiz.app.ui.screens.feedback

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.data.model.DogBreed
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme

@Composable
fun CorrectAnswerScreen(
    questionId: String,
    onContinue: () -> Unit = {},
    // In a real app, you'd get this data from a ViewModel
    breed: DogBreed = getSampleBreed(),
    pointsEarned: Int = 100
) {
    // Celebration animations
    val infiniteTransition = rememberInfiniteTransition(label = "celebration")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_animation"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation_animation"
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
                text = "🎉 ${stringResource(R.string.excellent)} 🎉",
                style = MaterialTheme.typography.displayMedium.copy(
                    color = DogBreedColors.SuccessGreen,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Animated Stars
            Text(
                text = "⭐ ⭐ ⭐ ⭐ ⭐",
                fontSize = 32.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.scale(scale)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Breed Name
            Text(
                text = "\"${breed.name}\"",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            
            // Points Earned
            Text(
                text = stringResource(R.string.points_earned, pointsEarned),
                style = MaterialTheme.typography.titleLarge.copy(
                    color = DogBreedColors.SuccessGreen,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
        }
        
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
                    .data(breed.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.dog_image_description),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        // Fun Fact Card
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
                    text = "💡 ${stringResource(R.string.did_you_know)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = breed.funFact,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DogBreedColors.DarkGray
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Additional Breed Info
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Origin",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DogBreedColors.SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = breed.origin,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DogBreedColors.DarkGray
                            )
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Size",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DogBreedColors.SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = breed.size.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DogBreedColors.DarkGray
                            )
                        )
                    }
                    
                    Column {
                        Text(
                            text = "Life Span",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = DogBreedColors.SecondaryText,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Text(
                            text = breed.lifeSpan,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = DogBreedColors.DarkGray
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Temperament",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DogBreedColors.SecondaryText,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = breed.temperament.joinToString(", "),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DogBreedColors.DarkGray
                    )
                )
            }
        }
        
        // Continue Button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DogBreedColors.SuccessGreen,
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

private fun getSampleBreed(): DogBreed {
    return DogBreed(
        id = "golden_retriever",
        name = "Golden Retriever",
        imageUrl = "https://images.unsplash.com/photo-1552053831-71594a27632d?w=400&h=300&fit=crop",
        description = "A friendly, intelligent, and devoted dog.",
        funFact = "Golden Retrievers were originally bred in Scotland for hunting waterfowl!",
        origin = "Scotland",
        size = DogBreed.Size.LARGE,
        temperament = listOf("Friendly", "Intelligent", "Devoted"),
        lifeSpan = "10-12 years",
        difficulty = DogBreed.Difficulty.BEGINNER
    )
}

@Preview(showBackground = true)
@Composable
fun CorrectAnswerScreenPreview() {
    DogBreedQuizTheme {
        CorrectAnswerScreen(questionId = "sample")
    }
}