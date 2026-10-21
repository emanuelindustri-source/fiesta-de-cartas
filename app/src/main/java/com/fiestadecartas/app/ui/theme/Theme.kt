package com.fiestadecartas.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.fiestadecartas.app.data.Prefs
import java.util.Calendar

val LocalFestival = staticCompositionLocalOf { Festival.FIESTA }
val LocalPalette = staticCompositionLocalOf { paletteFor(Festival.FIESTA) }

@Composable
fun FiestaTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val festival = Prefs.manualFestival
        .let { if (it == "auto") Festival.current() else Festival.fromKey(it) ?: Festival.current() }
    val base = paletteFor(festival)
    val systemDark = isSystemInDarkTheme()
    val dark = when (Prefs.themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemDark
    }
    var accent = base.accent
    if (Prefs.dynamicColor) {
        dynamicPrimary(context)?.let { accent = it }
    }
    val bg = if (dark) base.bgDark else base.bgLight
    val surface = if (dark) base.surfaceDark else base.surfaceLight
    val onBg = if (dark) Color(0xFFF6F1E7) else Color(0xFF1A1A1A)
    val scheme = if (dark) darkColorScheme(
        primary = accent,
        onPrimary = Color(0xFF101010),
        primaryContainer = accent.copy(alpha = 0.22f),
        onPrimaryContainer = accent,
        secondary = base.accent2,
        onSecondary = Color(0xFF0B0B0B),
        tertiary = base.accent3,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = surface.copy(alpha = 0.85f),
        onSurfaceVariant = onBg.copy(alpha = 0.72f),
        outline = accent.copy(alpha = 0.35f),
        error = Color(0xFFFF6B6B)
    ) else lightColorScheme(
        primary = accent,
        onPrimary = Color.White,
        primaryContainer = accent.copy(alpha = 0.16f),
        onPrimaryContainer = Color(0xFF2A1B00),
        secondary = base.accent2,
        onSecondary = Color.White,
        tertiary = base.accent3,
        background = bg,
        onBackground = onBg,
        surface = surface,
        onSurface = onBg,
        surfaceVariant = Color(0xFFF0E9DC),
        onSurfaceVariant = onBg.copy(alpha = 0.65f),
        outline = accent.copy(alpha = 0.4f),
        error = Color(0xFFB3261E)
    )
    val typography = Typography(
        displayLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 44.sp, letterSpacing = (-1).sp),
        displayMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, letterSpacing = (-0.5).sp),
        headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold, fontSize = 28.sp),
        headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 24.sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 0.2.sp),
        titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp),
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 23.sp),
        bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp),
        labelLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 14.sp, letterSpacing = 0.4.sp),
        labelSmall = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 11.sp, letterSpacing = 1.sp)
    )
    val view = LocalView.current
    LaunchedEffect(festival, Prefs.sound) {
        com.fiestadecartas.app.ui.Soundscape.sync(context, festival)
    }
    DisposableEffect(Unit) {
        onDispose { com.fiestadecartas.app.ui.Soundscape.stop() }
    }
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !dark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !dark
        }
    }
    CompositionLocalProvider(LocalFestival provides festival, LocalPalette provides base) {
        MaterialTheme(colorScheme = scheme, typography = typography, shapes = Shapes(
            extraSmall = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
            small = androidx.compose.foundation.shape.RoundedCornerShape(14.dp),
            medium = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
            large = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
            extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(36.dp)
        ), content = content)
    }
}

fun currentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
