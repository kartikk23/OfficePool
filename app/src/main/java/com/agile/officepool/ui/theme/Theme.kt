package com.agile.officepool.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.core.view.WindowCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF03DAC6),
    secondary = Color(0xFFBB86FC),
    tertiary = Color(0xFF3700B3),
    surface = Color(0xFF121212), // Dark surface color
    background = Color(0xFF000000),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF03DAC6),
    secondary = Color(0xFF6200EE),
    tertiary = Color(0xFF3700B3),
    surface = Color.White,
    background = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onSurface = Color.Black,
)


@Composable
fun OfficePoolTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    val context = LocalContext.current
    val systemUiController = rememberSystemUiController()

    SideEffect {
        val window = (context as? Activity)?.window
        window?.let {
            WindowCompat.setDecorFitsSystemWindows(it, false) // ✅ Enable edge-to-edge
        }

        systemUiController.setStatusBarColor(
            color = Color.Transparent, // Optional: transparent for immersive look
            darkIcons = !darkTheme
        )

        systemUiController.setNavigationBarColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
    ){
        CompositionLocalProvider(
            LocalTextStyle provides TextStyle(fontFamily = RobotoCondensed)
        ) {
            content()
        }
    }
}