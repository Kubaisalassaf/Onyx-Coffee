package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val OnyxDarkColorScheme = darkColorScheme(
    primary = OnyxGold,
    onPrimary = OnyxDeepBlack,
    primaryContainer = OnyxCoffeeBrownDark,
    onPrimaryContainer = OnyxWarmBeige,
    secondary = OnyxWarmBeige,
    onSecondary = OnyxDeepBlack,
    secondaryContainer = OnyxCardDark,
    onSecondaryContainer = OnyxCream,
    tertiary = OnyxGoldLight,
    onTertiary = OnyxDeepBlack,
    background = OnyxDeepBlack,
    onBackground = OnyxCream,
    surface = OnyxSurfaceDark,
    onSurface = OnyxCream,
    surfaceVariant = OnyxCardDark,
    onSurfaceVariant = OnyxMutedText,
    outline = OnyxCardBorder,
    outlineVariant = OnyxSubtle,
)

private val OnyxLightColorScheme = lightColorScheme(
    primary = OnyxCoffeeBrown,
    onPrimary = OnyxWhite,
    primaryContainer = OnyxWarmBeige,
    onPrimaryContainer = OnyxCoffeeBrownDark,
    secondary = OnyxGoldDark,
    onSecondary = OnyxWhite,
    secondaryContainer = OnyxCream,
    onSecondaryContainer = OnyxCoffeeBrown,
    tertiary = OnyxGold,
    onTertiary = OnyxDeepBlack,
    background = OnyxCream,
    onBackground = OnyxDeepBlack,
    surface = OnyxWhite,
    onSurface = OnyxDeepBlack,
    surfaceVariant = OnyxWarmBeige,
    onSurfaceVariant = OnyxCoffeeBrownDark,
    outline = OnyxGoldDark,
    outlineVariant = OnyxMutedText,
)

@Composable
fun OnyxCoffeeTheme(
    darkTheme: Boolean = true, // Luxury dark theme is signature for ONYX Coffee
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) OnyxDarkColorScheme else OnyxLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
