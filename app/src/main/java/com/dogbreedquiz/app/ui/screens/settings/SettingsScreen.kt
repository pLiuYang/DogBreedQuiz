@file:OptIn(ExperimentalMaterial3Api::class)

package com.dogbreedquiz.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dogbreedquiz.app.R
import com.dogbreedquiz.app.domain.model.DogBreed
import com.dogbreedquiz.app.domain.model.GameSettings
import com.dogbreedquiz.app.ui.theme.DogBreedColors
import com.dogbreedquiz.app.ui.theme.DogBreedQuizTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit = {}
) {
    // Mock settings - in real app, this would come from ViewModel
    var settings by remember {
        mutableStateOf(
            GameSettings(
                difficultyLevel = DogBreed.Difficulty.BEGINNER,
                questionTimer = 30,
                soundEffectsEnabled = true,
                hapticFeedbackEnabled = true,
                dailyRemindersEnabled = true,
                reminderTime = "18:00",
                achievementAlertsEnabled = true
            )
        )
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.settings),
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
            // Gameplay Settings
            item {
                SettingsSection(
                    title = stringResource(R.string.gameplay)
                ) {
                    // Difficulty Level
                    SettingsItem(
                        title = stringResource(R.string.difficulty_level),
                        subtitle = settings.difficultyLevel.name.lowercase()
                            .replaceFirstChar { it.uppercase() }
                    ) {
                        DifficultySelector(
                            currentDifficulty = settings.difficultyLevel,
                            onDifficultyChanged = { difficulty ->
                                settings = settings.copy(difficultyLevel = difficulty)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Question Timer
                    SettingsItem(
                        title = stringResource(R.string.question_timer),
                        subtitle = if (settings.questionTimer == 0) "No timer" else "${settings.questionTimer} seconds"
                    ) {
                        TimerSelector(
                            currentTimer = settings.questionTimer,
                            onTimerChanged = { timer ->
                                settings = settings.copy(questionTimer = timer)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Sound Effects
                    SettingsToggleItem(
                        title = stringResource(R.string.sound_effects),
                        isEnabled = settings.soundEffectsEnabled,
                        onToggle = { enabled ->
                            settings = settings.copy(soundEffectsEnabled = enabled)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Haptic Feedback
                    SettingsToggleItem(
                        title = stringResource(R.string.haptic_feedback),
                        isEnabled = settings.hapticFeedbackEnabled,
                        onToggle = { enabled ->
                            settings = settings.copy(hapticFeedbackEnabled = enabled)
                        }
                    )
                }
            }
            
            // Notification Settings
            item {
                SettingsSection(
                    title = stringResource(R.string.notifications)
                ) {
                    // Daily Reminders
                    SettingsToggleItem(
                        title = stringResource(R.string.daily_reminders),
                        subtitle = if (settings.dailyRemindersEnabled) settings.reminderTime else "Disabled",
                        isEnabled = settings.dailyRemindersEnabled,
                        onToggle = { enabled ->
                            settings = settings.copy(dailyRemindersEnabled = enabled)
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Achievement Alerts
                    SettingsToggleItem(
                        title = stringResource(R.string.achievement_alerts),
                        isEnabled = settings.achievementAlertsEnabled,
                        onToggle = { enabled ->
                            settings = settings.copy(achievementAlertsEnabled = enabled)
                        }
                    )
                }
            }
            
            // About Section
            item {
                SettingsSection(
                    title = "About"
                ) {
                    SettingsItem(
                        title = "App Version",
                        subtitle = "1.0.0"
                    ) { }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "View our privacy policy"
                    ) { }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    SettingsItem(
                        title = "Terms of Service",
                        subtitle = "View terms and conditions"
                    ) { }
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
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
                style = MaterialTheme.typography.titleMedium.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            content()
        }
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {}
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = DogBreedColors.DarkGray,
                        fontWeight = FontWeight.Medium
                    )
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = DogBreedColors.SecondaryText
                        )
                    )
                }
            }
        }
        
        content()
    }
}

@Composable
private fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = DogBreedColors.DarkGray,
                    fontWeight = FontWeight.Medium
                )
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = DogBreedColors.SecondaryText
                    )
                )
            }
        }
        
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = DogBreedColors.BackgroundWhite,
                checkedTrackColor = DogBreedColors.PrimaryBlue,
                uncheckedThumbColor = DogBreedColors.BackgroundWhite,
                uncheckedTrackColor = DogBreedColors.LightGray
            )
        )
    }
}

@Composable
private fun DifficultySelector(
    currentDifficulty: DogBreed.Difficulty,
    onDifficultyChanged: (DogBreed.Difficulty) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = currentDifficulty.name.lowercase().replaceFirstChar { it.uppercase() },
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DogBreedColors.PrimaryBlue,
                unfocusedBorderColor = DogBreedColors.LightGray
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DogBreed.Difficulty.values().forEach { difficulty ->
                DropdownMenuItem(
                    text = {
                        Text(difficulty.name.lowercase().replaceFirstChar { it.uppercase() })
                    },
                    onClick = {
                        onDifficultyChanged(difficulty)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun TimerSelector(
    currentTimer: Int,
    onTimerChanged: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val timerOptions = listOf(0, 15, 30, 45, 60)
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = if (currentTimer == 0) "No timer" else "$currentTimer seconds",
            onValueChange = { },
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DogBreedColors.PrimaryBlue,
                unfocusedBorderColor = DogBreedColors.LightGray
            )
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            timerOptions.forEach { timer ->
                DropdownMenuItem(
                    text = {
                        Text(if (timer == 0) "No timer" else "$timer seconds")
                    },
                    onClick = {
                        onTimerChanged(timer)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    DogBreedQuizTheme {
        SettingsScreen()
    }
}