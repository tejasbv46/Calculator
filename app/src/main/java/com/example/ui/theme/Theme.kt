package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CalculatorOrange,
    onPrimary = Color.White,
    secondary = MediumGrey,
    onSecondary = Color.White,
    tertiary = SoftSilver,
    onTertiary = Color.Black,
    background = DarkCharcoal,
    onBackground = Color.White,
    surface = SlateGrey,
    onSurface = Color.White,
    surfaceVariant = MediumGrey,
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = BrightOrange,
    onPrimary = Color.White,
    secondary = LightGrey,
    onSecondary = Color.Black,
    tertiary = SoftSilver,
    onTertiary = Color.Black,
    background = OffWhite,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = LightGrey,
    onSurfaceVariant = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to prefer our gorgeous hand-crafted color scheme
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
