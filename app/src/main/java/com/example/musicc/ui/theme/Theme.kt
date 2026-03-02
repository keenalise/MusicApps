package com.example.musicc.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SpotifyGreen,
    secondary = SpotifyGreen,
    tertiary = SpotifyGreen,
    background = SpotifyBlack,
    surface = SpotifyDarkGray,
    onPrimary = SpotifyBlack,
    onSecondary = SpotifyBlack,
    onTertiary = SpotifyBlack,
    onBackground = SpotifyWhite,
    onSurface = SpotifyWhite,
)

private val LightColorScheme = lightColorScheme(
    primary = SpotifyGreen,
    secondary = SpotifyGreen,
    tertiary = SpotifyGreen,
    background = SpotifyWhite,
    surface = SpotifyLightGray,
    onPrimary = SpotifyWhite,
    onSecondary = SpotifyWhite,
    onTertiary = SpotifyWhite,
    onBackground = SpotifyBlack,
    onSurface = SpotifyBlack,
)

@Composable
fun MusiccTheme(
    darkTheme: Boolean = true, // Default to dark theme like Spotify
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Disable for consistent Spotify look
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