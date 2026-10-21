package com.fiestadecartas.app.ui.theme

import android.content.Context
import android.os.Build
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import java.util.Calendar

enum class Festival(val key: String, val emoji: String, val title: String) {
    FIESTA("fiesta", "🎉", "Fiesta"),
    HALLOWEEN("halloween", "🎃", "Halloween"),
    CHRISTMAS("christmas", "🎄", "Navidad"),
    REYES("reyes", "👑", "Reyes"),
    VALENTINE("valentine", "❤️", "San Valentín"),
    EASTER("easter", "🐣", "Pascua"),
    SEMANA_SANTA("semana", "✝️", "Semana Santa");

    companion object {
        fun fromKey(key: String?): Festival? = entries.firstOrNull { it.key == key }

        fun current(calendar: Calendar = Calendar.getInstance()): Festival {
            val month = calendar.get(Calendar.MONTH) + 1
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val year = calendar.get(Calendar.YEAR)

            if (month == 12 && day >= 1) return CHRISTMAS
            if (month == 1 && day <= 2) return CHRISTMAS
            if (month == 1 && day in 3..6) return REYES
            if (month == 2 && day in 7..14) return VALENTINE

            val easter = easterDate(year)
            val easterDay = dayOfYear(year, easter.first, easter.second)
            val today = calendar.get(Calendar.DAY_OF_YEAR)
            if (today in (easterDay - 7) until easterDay) return SEMANA_SANTA
            if (today in easterDay..(easterDay + 7)) return EASTER

            if (month == 10 && day >= 25) return HALLOWEEN
            if (month == 11 && day <= 2) return HALLOWEEN
            return FIESTA
        }

        fun easterDate(year: Int): Pair<Int, Int> {
            val a = year % 19
            val b = year / 100
            val c = year % 100
            val d = b / 4
            val e = b % 4
            val f = (b + 8) / 25
            val g = (b - f + 1) / 3
            val h = (19 * a + b - d - g + 15) % 30
            val i = c / 4
            val k = c % 4
            val l = (32 + 2 * e + 2 * i - h - k) % 7
            val m = (a + 11 * h + 22 * l) / 451
            val month = (h + l - 7 * m + 114) / 31
            val day = ((h + l - 7 * m + 114) % 31) + 1
            return month to day
        }

        private fun dayOfYear(year: Int, month: Int, day: Int): Int {
            val c = Calendar.getInstance()
            c.set(year, month - 1, day)
            return c.get(Calendar.DAY_OF_YEAR)
        }

        /** Days until the next seasonal window starts (0 when active). */
        fun daysToNext(now: Calendar = Calendar.getInstance()): Pair<Festival, Int> {
            val today = current(now)
            val c = now.clone() as Calendar
            for (offset in 1..400) {
                c.timeInMillis = now.timeInMillis
                c.add(Calendar.DAY_OF_YEAR, offset)
                val f = current(c)
                if (f != today) return f to offset
            }
            return FIESTA to 0
        }
    }
}

data class FestivalPalette(
    val accent: Color,
    val accent2: Color,
    val accent3: Color,
    val bgDark: Color,
    val surfaceDark: Color,
    val bgLight: Color,
    val surfaceLight: Color,
    val glow: Color
)

fun paletteFor(festival: Festival): FestivalPalette = when (festival) {
    Festival.FIESTA -> FestivalPalette(
        Color(0xFFFFB703), Color(0xFF2EC4B6), Color(0xFFFF6B6B),
        Color(0xFF0C1A26), Color(0xFF14293A), Color(0xFFFFF8EC), Color(0xFFFFFFFF), Color(0xFFFFD166)
    )
    Festival.HALLOWEEN -> FestivalPalette(
        Color(0xFFFF7A00), Color(0xFF9D4EDD), Color(0xFF7BF1A8),
        Color(0xFF110A1B), Color(0xFF1E1230), Color(0xFFFFF3E4), Color(0xFFFFFFFF), Color(0xFFFF9E40)
    )
    Festival.CHRISTMAS -> FestivalPalette(
        Color(0xFFE63946), Color(0xFF2A9D8F), Color(0xFFFFD166),
        Color(0xFF081A13), Color(0xFF102A20), Color(0xFFFFF6F1), Color(0xFFFFFFFF), Color(0xFFFF6B6B)
    )
    Festival.REYES -> FestivalPalette(
        Color(0xFFFFD166), Color(0xFF7B2CBF), Color(0xFF4CC9F0),
        Color(0xFF130C26), Color(0xFF231744), Color(0xFFFFF9E8), Color(0xFFFFFFFF), Color(0xFFFFE08A)
    )
    Festival.VALENTINE -> FestivalPalette(
        Color(0xFFFF4D6D), Color(0xFFC9184A), Color(0xFFFFB3C1),
        Color(0xFF190811), Color(0xFF2B1220), Color(0xFFFFF0F3), Color(0xFFFFFFFF), Color(0xFFFF8FA3)
    )
    Festival.EASTER -> FestivalPalette(
        Color(0xFF6FD8D0), Color(0xFFCDB4DB), Color(0xFFFFE08A),
        Color(0xFF0B1C24), Color(0xFF15303A), Color(0xFFF3FBF8), Color(0xFFFFFFFF), Color(0xFFA8E6CF)
    )
    Festival.SEMANA_SANTA -> FestivalPalette(
        Color(0xFFD4AF37), Color(0xFF6A4C93), Color(0xFFE9D8A6),
        Color(0xFF130F1F), Color(0xFF211A33), Color(0xFFFAF6EC), Color(0xFFFFFFFF), Color(0xFFD4AF37)
    )
}

/** True when the user allowed wallpaper-based accents. */
fun wallpaperAccent(context: Context): Color? = runCatching {
    val wm = android.app.WallpaperManager.getInstance(context)
    val drawable = wm.drawable ?: return null
    val bmp = drawable.toBitmap(64, 64)
    val palette = androidx.palette.graphics.Palette.from(bmp).generate()
    val swatches = listOfNotNull(palette.vibrantSwatch, palette.dominantSwatch, palette.mutedSwatch)
    swatches.firstOrNull { sw ->
        val c = Color(sw.rgb)
        val lum = 0.2126f * c.red + 0.7152f * c.green + 0.0722f * c.blue
        lum in 0.25f..0.8f
    }?.let { Color(it.rgb) }
}.getOrNull()

fun dynamicPrimary(context: Context): Color? =
    if (Build.VERSION.SDK_INT >= 31) {
        runCatching {
            val scheme = android.provider.Settings.Secure.getString(
                context.contentResolver, "theme_customization_overlay_packages"
            )
            if (scheme.isNullOrBlank()) null else wallpaperAccent(context)
        }.getOrNull()
    } else wallpaperAccent(context)
