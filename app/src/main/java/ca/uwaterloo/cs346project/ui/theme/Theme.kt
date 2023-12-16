package ca.uwaterloo.cs346project.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.ColorScheme
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
import ca.uwaterloo.cs346project.MainActivity

val DarkColorScheme = darkColorScheme()
val LightColorScheme = lightColorScheme()

@Composable
fun Cs346projectTheme(
        darkTheme: Boolean = MainActivity.settings.darkMode,
        // Dynamic color is available on Android 12+
        dynamicColor: Boolean = true,
        content: @Composable () -> Unit
) {
    val colorScheme = getColorScheme(true, darkTheme)
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

@Composable
fun getColorScheme(dynamicColor: Boolean = true, useDarkTheme: Boolean = false): ColorScheme {
    when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDarkTheme) return dynamicDarkColorScheme(context) else return dynamicLightColorScheme(context)
        }

        useDarkTheme -> return DarkColorScheme
        else -> return LightColorScheme
    }
}