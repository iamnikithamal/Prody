package com.prody.prashant.ui.theme

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    // Primary colors
    primary = PrimarySage,
    onPrimary = Color.White,
    primaryContainer = PrimarySageLight,
    onPrimaryContainer = PrimarySageDark,

    // Secondary colors
    secondary = TertiaryGold,
    onSecondary = Color.White,
    secondaryContainer = TertiaryGoldLight,
    onSecondaryContainer = TertiaryGoldDark,

    // Tertiary colors
    tertiary = AccentInfo,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFD6E3FF),
    onTertiaryContainer = Color(0xFF001B3E),

    // Error colors
    error = AccentError,
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    // Background and Surface
    background = SurfaceLight,
    onBackground = NeutralBlack,
    surface = SurfaceLight,
    onSurface = NeutralBlack,
    surfaceVariant = SurfaceContainerLight,
    onSurfaceVariant = NeutralDark,

    // Surface containers
    surfaceContainerLowest = NeutralWhite,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = NeutralLight,

    // Outline
    outline = NeutralMedium,
    outlineVariant = NeutralLight,

    // Inverse
    inverseSurface = NeutralBlack,
    inverseOnSurface = NeutralWhite,
    inversePrimary = PrimarySageLight,

    // Scrim
    scrim = Color.Black
)

private val DarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimarySageLight,
    onPrimary = PrimarySageDark,
    primaryContainer = PrimarySage,
    onPrimaryContainer = Color(0xFFD4E8DA),

    // Secondary colors
    secondary = TertiaryGoldLight,
    onSecondary = TertiaryGoldDark,
    secondaryContainer = TertiaryGold,
    onSecondaryContainer = Color(0xFFF5E6D3),

    // Tertiary colors
    tertiary = Color(0xFFADC6FF),
    onTertiary = Color(0xFF002E64),
    tertiaryContainer = AccentInfo,
    onTertiaryContainer = Color(0xFFD6E3FF),

    // Error colors
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = AccentError,
    onErrorContainer = Color(0xFFFFDAD6),

    // Background and Surface
    background = SurfaceDark,
    onBackground = NeutralOffWhite,
    surface = SurfaceDark,
    onSurface = NeutralOffWhite,
    surfaceVariant = SurfaceContainerDark,
    onSurfaceVariant = NeutralLight,

    // Surface containers
    surfaceContainerLowest = Color(0xFF131110),
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = Color(0xFF3A3735),

    // Outline
    outline = NeutralMedium,
    outlineVariant = NeutralDark,

    // Inverse
    inverseSurface = NeutralOffWhite,
    inverseOnSurface = NeutralBlack,
    inversePrimary = PrimarySage,

    // Scrim
    scrim = Color.Black
)

@Composable
fun ProdiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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
            WindowCompat.setDecorFitsSystemWindows(window, false)
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
