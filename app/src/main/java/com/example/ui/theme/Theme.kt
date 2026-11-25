package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CrushDarkColorScheme = darkColorScheme(
    primary = CrushRed,
    onPrimary = CrushWhite,
    primaryContainer = CrushRedDark,
    onPrimaryContainer = CrushYellowBright,
    secondary = CrushYellow,
    onSecondary = CrushBlack,
    secondaryContainer = CrushElevatedSurface,
    onSecondaryContainer = CrushYellowBright,
    tertiary = CrushYellowDeep,
    onTertiary = CrushBlack,
    background = CrushBlack,
    onBackground = CrushWhite,
    surface = CrushCharcoal,
    onSurface = CrushWhite,
    surfaceVariant = CrushCardSurface,
    onSurfaceVariant = CrushMutedText,
    outline = CrushYellow
)

private val CrushLightColorScheme = lightColorScheme(
    primary = CrushRed,
    onPrimary = CrushWhite,
    primaryContainer = CrushRedDark,
    onPrimaryContainer = CrushWhite,
    secondary = CrushYellow,
    onSecondary = CrushBlack,
    secondaryContainer = CrushYellowBright,
    onSecondaryContainer = CrushBlack,
    tertiary = CrushBlack,
    onTertiary = CrushYellow,
    background = CrushBlack,
    onBackground = CrushWhite,
    surface = CrushCharcoal,
    onSurface = CrushWhite,
    surfaceVariant = CrushCardSurface,
    onSurfaceVariant = CrushMutedText,
    outline = CrushYellow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) CrushDarkColorScheme else CrushLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
