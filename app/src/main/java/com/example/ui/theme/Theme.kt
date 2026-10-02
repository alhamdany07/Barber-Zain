package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class AppThemeColor {
    NAVY,
    TEAL,
    AMBER,
    SLATE
}

private fun getLightColorScheme(themeColor: AppThemeColor) = lightColorScheme(
    primary = when (themeColor) {
        AppThemeColor.NAVY -> PrimaryNavy
        AppThemeColor.TEAL -> AccentTeal
        AppThemeColor.AMBER -> AccentAmber
        AppThemeColor.SLATE -> Color(0xFF334155)
    },
    onPrimary = Color.White,
    primaryContainer = TintNavy,
    onPrimaryContainer = PrimaryNavy,
    secondary = AccentAmber,
    onSecondary = Color.White,
    secondaryContainer = TintAmber,
    onSecondaryContainer = Color(0xFFB45309),
    tertiary = AccentGreen,
    background = AppBgLight,
    surface = CardBgLight,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFEDF0F7),
    onSurfaceVariant = TextSecondaryLight,
    outline = Color(0xFFE2E8F0),
    error = AccentRed,
    onError = Color.White,
    errorContainer = TintRed,
    onErrorContainer = AccentRed
)

private fun getDarkColorScheme(themeColor: AppThemeColor) = darkColorScheme(
    primary = when (themeColor) {
        AppThemeColor.NAVY -> Color(0xFF818CF8)
        AppThemeColor.TEAL -> Color(0xFF2DD4BF)
        AppThemeColor.AMBER -> Color(0xFFFBBF24)
        AppThemeColor.SLATE -> Color(0xFF94A3B8)
    },
    onPrimary = Color(0xFF0F1424),
    primaryContainer = Color(0xFF2E3F9E),
    onPrimaryContainer = Color.White,
    secondary = AccentAmber,
    onSecondary = Color(0xFF0F1424),
    background = AppBgDark,
    surface = CardBgDark,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    surfaceVariant = Color(0xFF252D4A),
    onSurfaceVariant = TextSecondaryDark,
    outline = Color(0xFF333F63),
    error = AccentRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeColor: AppThemeColor = AppThemeColor.NAVY,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        getDarkColorScheme(themeColor)
    } else {
        getLightColorScheme(themeColor)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
