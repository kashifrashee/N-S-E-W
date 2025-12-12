package com.kashif.myapplication.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dark Theme Palette
private val DarkColorScheme = darkColorScheme(
    primary = CompassAccent,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color.White,
    onBackground = DarkTextPrimary,
    onSurface = DarkTextSecondary
)

// Light Theme Palette
private val LightColorScheme = lightColorScheme(
    primary = CompassAccent,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onBackground = LightTextPrimary,
    onSurface = LightTextSecondary
)

@Composable
fun NSEWTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color ko hum FALSE kar rahe hain taake aapka design kharab na ho
    // Agar TRUE rakhenge to user ke wallpaper ke hisab se color change ho jayega
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // Agar aapko phir bhi dynamic colors chahiye to ise uncomment kar sakte hain
        // dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        //     val context = LocalContext.current
        //     if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        // }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // --- STATUS BAR LOGIC START ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            // Status bar ka background color same app ke background jesa set kr dia
            window.statusBarColor = colorScheme.background.toArgb()

            // Yahan magic hota hai:
            // Agar Dark Theme hai -> Icons LIGHT honge (!darkTheme = false)
            // Agar Light Theme hai -> Icons DARK honge (!darkTheme = true)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    // --- STATUS BAR LOGIC END ---

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Make sure Typography.kt file exist krti ho default wali
        content = content
    )
}