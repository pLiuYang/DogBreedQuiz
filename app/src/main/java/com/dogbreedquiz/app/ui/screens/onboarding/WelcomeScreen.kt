package com.dogbreedquiz.app.ui.screens.onboarding

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
fun WelcomeScreen(
    onGetStarted: () -> Unit = {},
    onLearnMore: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dog_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "scale_animation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // App Title and Icon
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f)
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            
            // Animated Dog Icon (placeholder for now)
            Card(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                shape = RoundedCornerShape(60.dp),
                colors = CardDefaults.cardColors(
                    containerColor = DogBreedColors.LightBlue
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🐕",
                        fontSize = 48.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayLarge.copy(
                    color = DogBreedColors.PrimaryBlue,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.headlineMedium.copy(
                    color = DogBreedColors.DarkGray
                ),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.welcome_description),
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = DogBreedColors.SecondaryText
                ),
                textAlign = TextAlign.Center
            )
        }
        
        // Action Buttons
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = onGetStarted,
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
                    text = stringResource(R.string.get_started),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
            
            OutlinedButton(
                onClick = onLearnMore,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = DogBreedColors.PrimaryBlue
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 2.dp
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.learn_more),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    DogBreedQuizTheme {
        WelcomeScreen()
    }
}