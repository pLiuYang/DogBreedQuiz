package com.dogbreedquiz.app.ui.screens.feedback

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme

@Composable
fun IncorrectAnswerScreen(
    questionId: String,
    onContinue: () -> Unit = {},
    // In a real app, you'd get this data from a ViewModel
    correctBreed: DogBreed = getSampleBreed(),
    selectedBreed: DogBreed = getSampleIncorrectBreed()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Gentle Feedback Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🤔 ${stringResource(R.string.not_quite_right)}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = DogBreedColors.WarningOrange,
                    fontWeight = FontWeight.SemiBold
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = stringResource(R.string.correct_answer_is),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DogBreedColors.SecondaryText
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "\"${correctBreed.name}\"",
                style = MaterialTheme.typography.headlineMedium.copy(
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
                    .data(correctBreed.imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.dog_image_description),
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
        }
        
        // Educational Content Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = DogBreedColors.LightBlue
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "📚 ${stringResource(R.string.learn_difference)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = getBreedComparison(correctBreed, selectedBreed),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = DogBreedColors.DarkGray
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Breed Information Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Correct Breed Info
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DogBreedColors.LightGreen
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(DogBreedColors.SuccessGreen)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✓ ${correctBreed.name}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = DogBreedColors.SuccessGreen,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Origin: ${correctBreed.origin}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.DarkGray
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Size: ${correctBreed.size.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.DarkGray
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            // Selected Breed Info
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DogBreedColors.LightRed
                ),
                border = CardDefaults.outlinedCardBorder().copy(
                    width = 2.dp,
                    brush = androidx.compose.ui.graphics.SolidColor(DogBreedColors.ErrorRed)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "✗ ${selectedBreed.name}",
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = DogBreedColors.ErrorRed,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Origin: ${selectedBreed.origin}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.DarkGray
                        ),
                        textAlign = TextAlign.Center
                    )
                    
                    Text(
                        text = "Size: ${selectedBreed.size.name.lowercase().replaceFirstChar { it.uppercase() }}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.DarkGray
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Encouragement Message
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    text = "💪 Keep Learning!",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = DogBreedColors.PrimaryBlue,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Every mistake is a learning opportunity. You're getting better with each question!",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DogBreedColors.SecondaryText
                    ),
                    textAlign = TextAlign.Center
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
                containerColor = DogBreedColors.PrimaryBlue,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = stringResource(R.string.got_it),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            )
        }
    }
}

private fun getBreedComparison(correctBreed: DogBreed, selectedBreed: DogBreed): String {
    return when {
        correctBreed.name == "Golden Retriever" && selectedBreed.name == "Labrador Retriever" ->
            "Golden Retrievers have longer, wavier coats than Labradors and feathery tails. Labs have shorter, water-resistant coats."
        
        correctBreed.name == "Labrador Retriever" && selectedBreed.name == "Golden Retriever" ->
            "Labradors have shorter, water-resistant coats compared to Golden Retrievers' longer, wavier fur. Labs also have otter-like tails."
        
        correctBreed.name == "German Shepherd" && selectedBreed.name == "Belgian Malinois" ->
            "German Shepherds are larger and have a more sloped back. Belgian Malinois are more compact and have a straighter topline."
        
        correctBreed.size != selectedBreed.size ->
            "${correctBreed.name}s are ${correctBreed.size.name.lowercase()} dogs, while ${selectedBreed.name}s are ${selectedBreed.size.name.lowercase()}. Size is often a key distinguishing feature!"
        
        correctBreed.origin != selectedBreed.origin ->
            "${correctBreed.name}s originated in ${correctBreed.origin}, while ${selectedBreed.name}s come from ${selectedBreed.origin}. Different origins often mean different breeding purposes!"
        
        else ->
            "${correctBreed.name}s have distinctive features like ${correctBreed.temperament.first().lowercase()} temperament. Look for unique characteristics that set each breed apart!"
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

private fun getSampleIncorrectBreed(): DogBreed {
    return DogBreed(
        id = "labrador_retriever",
        name = "Labrador Retriever",
        imageUrl = "https://images.unsplash.com/photo-1518717758536-85ae29035b6d?w=400&h=300&fit=crop",
        description = "Labs are friendly, outgoing, and active companions.",
        funFact = "Labradors are the most popular dog breed in the United States!",
        origin = "Newfoundland, Canada",
        size = DogBreed.Size.LARGE,
        temperament = listOf("Outgoing", "Even Tempered", "Gentle"),
        lifeSpan = "10-12 years",
        difficulty = DogBreed.Difficulty.BEGINNER
    )
}

@Preview(showBackground = true)
@Composable
fun IncorrectAnswerScreenPreview() {
    DogBreedQuizTheme {
        IncorrectAnswerScreen(questionId = "sample")
    }
}