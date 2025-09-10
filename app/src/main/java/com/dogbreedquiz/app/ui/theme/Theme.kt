package com.dogbreedquiz.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = DogBreedColors.PrimaryBlue,
    secondary = DogBreedColors.SuccessGreen,
    tertiary = DogBreedColors.WarningOrange,
    background = DogBreedColors.DarkGray,
    surface = DogBreedColors.MediumGray,
    onPrimary = DogBreedColors.BackgroundWhite,
    onSecondary = DogBreedColors.BackgroundWhite,
    onTertiary = DogBreedColors.BackgroundWhite,
    onBackground = DogBreedColors.BackgroundWhite,
    onSurface = DogBreedColors.BackgroundWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = DogBreedColors.PrimaryBlue,
    onPrimary = DogBreedColors.BackgroundWhite,
    primaryContainer = DogBreedColors.LightBlue,
    onPrimaryContainer = DogBreedColors.DarkGray,
    secondary = DogBreedColors.SuccessGreen,
    onSecondary = DogBreedColors.BackgroundWhite,
    secondaryContainer = DogBreedColors.LightGreen,
    onSecondaryContainer = DogBreedColors.DarkGray,
    tertiary = DogBreedColors.WarningOrange,
    onTertiary = DogBreedColors.BackgroundWhite,
    tertiaryContainer = DogBreedColors.LightOrange,
    onTertiaryContainer = DogBreedColors.DarkGray,
    error = DogBreedColors.ErrorRed,
    errorContainer = DogBreedColors.LightRed,
    onError = DogBreedColors.BackgroundWhite,
    onErrorContainer = DogBreedColors.DarkGray,
    background = DogBreedColors.BackgroundWhite,
    onBackground = DogBreedColors.DarkGray,
    surface = DogBreedColors.BackgroundWhite,
    onSurface = DogBreedColors.DarkGray,
    surfaceVariant = DogBreedColors.OffWhite,
    onSurfaceVariant = DogBreedColors.MediumGray,
    outline = DogBreedColors.LightGray,
    inverseOnSurface = DogBreedColors.BackgroundWhite,
    inverseSurface = DogBreedColors.DarkGray,
    inversePrimary = DogBreedColors.LightBlue,
    surfaceTint = DogBreedColors.PrimaryBlue,
    outlineVariant = DogBreedColors.BorderDefault,
    scrim = DogBreedColors.DarkGray,
)

@Composable
fun DogBreedQuizTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}