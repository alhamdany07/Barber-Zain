package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeColor {
    NAVY,
    TEAL,
    AMBER,
    SLATE
}

@Immutable
data class AppExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val successContainer: Color,
    val onSuccessContainer: Color,
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
    val info: Color,
    val onInfo: Color,
    val infoContainer: Color,
    val onInfoContainer: Color,
    // Queue Status Colors (contrast >= 4.5:1 text on background)
    val statusWaiting: Color,
    val statusWaitingBg: Color,
    val statusCalled: Color,
    val statusCalledBg: Color,
    val statusInService: Color,
    val statusInServiceBg: Color,
    val statusCompleted: Color,
    val statusCompletedBg: Color,
    val statusCancelled: Color,
    val statusCancelledBg: Color,
    // Hero Card Gradient (follows active theme)
    val heroGradientStart: Color,
    val heroGradientEnd: Color,
    // Subtle border for cards & chips
    val borderSubtle: Color
)

private fun getLightColorScheme(themeColor: AppThemeColor): ColorScheme = when (themeColor) {
    AppThemeColor.NAVY -> lightColorScheme(
        primary = Color(0xFF1E2A78),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFEEF2FF),
        onPrimaryContainer = Color(0xFF1E2A78),
        secondary = Color(0xFFD97706),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF92400E),
        tertiary = Color(0xFF047857),
        background = AppBgLight,
        surface = CardBgLight,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        surfaceVariant = Color(0xFFEDF0F7),
        onSurfaceVariant = TextSecondaryLight,
        outline = Color(0xFFE2E8F0),
        outlineVariant = Color(0x1A1E2A78),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B)
    )
    AppThemeColor.TEAL -> lightColorScheme(
        primary = Color(0xFF0D9488),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFCCFBF1),
        onPrimaryContainer = Color(0xFF0F766E),
        secondary = Color(0xFFD97706),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF92400E),
        tertiary = Color(0xFF2563EB),
        background = AppBgLight,
        surface = CardBgLight,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        surfaceVariant = Color(0xFFEDF2F2),
        onSurfaceVariant = TextSecondaryLight,
        outline = Color(0xFFE2E8F0),
        outlineVariant = Color(0x1A0D9488),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B)
    )
    AppThemeColor.AMBER -> lightColorScheme(
        primary = Color(0xFFB45309),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFEF3C7),
        onPrimaryContainer = Color(0xFF92400E),
        secondary = Color(0xFF1E2A78),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFEEF2FF),
        onSecondaryContainer = Color(0xFF1E2A78),
        tertiary = Color(0xFF047857),
        background = AppBgLight,
        surface = CardBgLight,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        surfaceVariant = Color(0xFFF8F5EE),
        onSurfaceVariant = TextSecondaryLight,
        outline = Color(0xFFE2E8F0),
        outlineVariant = Color(0x1AB45309),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B)
    )
    AppThemeColor.SLATE -> lightColorScheme(
        primary = Color(0xFF334155),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF1F5F9),
        onPrimaryContainer = Color(0xFF0F172A),
        secondary = Color(0xFFD97706),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFFEF3C7),
        onSecondaryContainer = Color(0xFF92400E),
        tertiary = Color(0xFF2563EB),
        background = AppBgLight,
        surface = CardBgLight,
        onBackground = TextPrimaryLight,
        onSurface = TextPrimaryLight,
        surfaceVariant = Color(0xFFF1F5F9),
        onSurfaceVariant = TextSecondaryLight,
        outline = Color(0xFFE2E8F0),
        outlineVariant = Color(0x1A334155),
        error = Color(0xFFDC2626),
        onError = Color.White,
        errorContainer = Color(0xFFFEE2E2),
        onErrorContainer = Color(0xFF991B1B)
    )
}

private fun getDarkColorScheme(themeColor: AppThemeColor): ColorScheme = when (themeColor) {
    AppThemeColor.NAVY -> darkColorScheme(
        primary = Color(0xFF818CF8),
        onPrimary = Color(0xFF0F172A),
        primaryContainer = Color(0xFF2E3F9E),
        onPrimaryContainer = Color(0xFFEEF2FF),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF451A03),
        background = AppBgDark,
        surface = CardBgDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF252D4A),
        onSurfaceVariant = TextSecondaryDark,
        outline = Color(0xFF333F63),
        outlineVariant = Color(0x26818CF8),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA)
    )
    AppThemeColor.TEAL -> darkColorScheme(
        primary = Color(0xFF2DD4BF),
        onPrimary = Color(0xFF042F2E),
        primaryContainer = Color(0xFF115E59),
        onPrimaryContainer = Color(0xFFCCFBF1),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF451A03),
        background = AppBgDark,
        surface = CardBgDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF1D2C34),
        onSurfaceVariant = TextSecondaryDark,
        outline = Color(0xFF24444C),
        outlineVariant = Color(0x262DD4BF),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA)
    )
    AppThemeColor.AMBER -> darkColorScheme(
        primary = Color(0xFFFBBF24),
        onPrimary = Color(0xFF451A03),
        primaryContainer = Color(0xFF78350F),
        onPrimaryContainer = Color(0xFFFEF3C7),
        secondary = Color(0xFF818CF8),
        onSecondary = Color(0xFF0F172A),
        background = AppBgDark,
        surface = CardBgDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF2E261E),
        onSurfaceVariant = TextSecondaryDark,
        outline = Color(0xFF4C3E28),
        outlineVariant = Color(0x26FBBF24),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA)
    )
    AppThemeColor.SLATE -> darkColorScheme(
        primary = Color(0xFF94A3B8),
        onPrimary = Color(0xFF0F172A),
        primaryContainer = Color(0xFF334155),
        onPrimaryContainer = Color(0xFFF8FAFC),
        secondary = Color(0xFFFBBF24),
        onSecondary = Color(0xFF451A03),
        background = AppBgDark,
        surface = CardBgDark,
        onBackground = TextPrimaryDark,
        onSurface = TextPrimaryDark,
        surfaceVariant = Color(0xFF242A38),
        onSurfaceVariant = TextSecondaryDark,
        outline = Color(0xFF394356),
        outlineVariant = Color(0x2694A3B8),
        error = Color(0xFFF87171),
        onError = Color(0xFF450A0A),
        errorContainer = Color(0xFF7F1D1D),
        onErrorContainer = Color(0xFFFECACA)
    )
}

private fun getAppExtendedColors(darkTheme: Boolean, themeColor: AppThemeColor): AppExtendedColors {
    val (heroStart, heroEnd) = if (!darkTheme) {
        when (themeColor) {
            AppThemeColor.NAVY -> Pair(Color(0xFF1E2A78), Color(0xFF3B52C7))
            AppThemeColor.TEAL -> Pair(Color(0xFF0F766E), Color(0xFF14B8A6))
            AppThemeColor.AMBER -> Pair(Color(0xFFB45309), Color(0xFFF59E0B))
            AppThemeColor.SLATE -> Pair(Color(0xFF1E293B), Color(0xFF475569))
        }
    } else {
        when (themeColor) {
            AppThemeColor.NAVY -> Pair(Color(0xFF1E293B), Color(0xFF312E81))
            AppThemeColor.TEAL -> Pair(Color(0xFF042F2E), Color(0xFF0F766E))
            AppThemeColor.AMBER -> Pair(Color(0xFF451A03), Color(0xFF92400E))
            AppThemeColor.SLATE -> Pair(Color(0xFF0F172A), Color(0xFF1E293B))
        }
    }

    return if (!darkTheme) {
        AppExtendedColors(
            success = Color(0xFF047857),
            onSuccess = Color.White,
            successContainer = Color(0xFFD1FAE5),
            onSuccessContainer = Color(0xFF065F46),
            warning = Color(0xFFB45309),
            onWarning = Color.White,
            warningContainer = Color(0xFFFEF3C7),
            onWarningContainer = Color(0xFF92400E),
            info = Color(0xFF1D4ED8),
            onInfo = Color.White,
            infoContainer = Color(0xFFDBEAFE),
            onInfoContainer = Color(0xFF1E40AF),
            // Queue status colors: CALLED is distinctly violet, IN_SERVICE is blue, both contrast >= 4.5:1
            statusWaiting = Color(0xFFB45309),
            statusWaitingBg = Color(0xFFFEF3C7),
            statusCalled = Color(0xFF6D28D9),
            statusCalledBg = Color(0xFFEDE9FE),
            statusInService = Color(0xFF1D4ED8),
            statusInServiceBg = Color(0xFFDBEAFE),
            statusCompleted = Color(0xFF047857),
            statusCompletedBg = Color(0xFFD1FAE5),
            statusCancelled = Color(0xFFB91C1C),
            statusCancelledBg = Color(0xFFFEE2E2),
            heroGradientStart = heroStart,
            heroGradientEnd = heroEnd,
            borderSubtle = Color(0x14000000)
        )
    } else {
        AppExtendedColors(
            success = Color(0xFF34D399),
            onSuccess = Color(0xFF064E3B),
            successContainer = Color(0xFF064E3B),
            onSuccessContainer = Color(0xFFA7F3D0),
            warning = Color(0xFFFBBF24),
            onWarning = Color(0xFF451A03),
            warningContainer = Color(0xFF78350F),
            onWarningContainer = Color(0xFFFDE68A),
            info = Color(0xFF60A5FA),
            onInfo = Color(0xFF1E3A8A),
            infoContainer = Color(0xFF1E3A8A),
            onInfoContainer = Color(0xFFBFDBFE),
            // Queue status in dark mode (contrast >= 4.5:1)
            statusWaiting = Color(0xFFFDE68A),
            statusWaitingBg = Color(0xFF78350F),
            statusCalled = Color(0xFFDDD6FE),
            statusCalledBg = Color(0xFF4C1D95),
            statusInService = Color(0xFFBFDBFE),
            statusInServiceBg = Color(0xFF1E3A8A),
            statusCompleted = Color(0xFFA7F3D0),
            statusCompletedBg = Color(0xFF064E3B),
            statusCancelled = Color(0xFFFECACA),
            statusCancelledBg = Color(0xFF7F1D1D),
            heroGradientStart = heroStart,
            heroGradientEnd = heroEnd,
            borderSubtle = Color(0x26FFFFFF)
        )
    }
}

val LocalAppExtendedColors = staticCompositionLocalOf {
    getAppExtendedColors(darkTheme = false, themeColor = AppThemeColor.NAVY)
}

val MaterialTheme.extendedColors: AppExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppExtendedColors.current

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

    val extendedColors = getAppExtendedColors(darkTheme = darkTheme, themeColor = themeColor)

    CompositionLocalProvider(LocalAppExtendedColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
