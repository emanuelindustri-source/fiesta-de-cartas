package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fiestadecartas.app.R

data class Achievement(
    val id: String,
    val titleRes: Int,
    val target: Int,
    val reward: Int,
    val icon: String,
    val metric: String
)

object Achievements {
    private lateinit var sp: SharedPreferences
    var claimed by mutableStateOf<Set<String>>(emptySet())
        private set

    val all = listOf(
        Achievement("first_win", R.string.ach_first_win, 1, 60, "🥇", "wins"),
        Achievement("wins_10", R.string.ach_wins_10, 10, 120, "🏆", "wins"),
        Achievement("wins_50", R.string.ach_wins_50, 50, 300, "👑", "wins"),
        Achievement("played_25", R.string.ach_played_25, 25, 150, "🎲", "played"),
        Achievement("caida_5", R.string.ach_caida_5, 5, 100, "🃏", "caida_wins"),
        Achievement("ludo_5", R.string.ach_ludo_5, 5, 100, "🎯", "ludo_wins"),
        Achievement("fiesta_5", R.string.ach_fiesta_5, 5, 100, "🎨", "fiesta_wins"),
        Achievement("kemix_5", R.string.ach_kemix_5, 5, 100, "🪷", "kemix_wins"),
        Achievement("nervioso_5", R.string.ach_nervioso_5, 5, 100, "✋", "nervioso_wins"),
        Achievement("level_10", R.string.ach_level_10, 10, 200, "⭐", "level")
    )

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_achievements", Context.MODE_PRIVATE)
        claimed = sp.getStringSet("claimed", emptySet())?.toSet() ?: emptySet()
    }

    fun progress(achievement: Achievement): Int = when (achievement.metric) {
        "wins" -> GameStats.totalWins
        "played" -> GameStats.totalPlayed
        "caida_wins" -> GameStats.statFor("caida").wins
        "ludo_wins" -> GameStats.statFor("ludo").wins
        "fiesta_wins" -> GameStats.statFor("fiesta").wins
        "kemix_wins" -> GameStats.statFor("kemix").wins
        "nervioso_wins" -> GameStats.statFor("nervioso").wins
        "domino_wins" -> GameStats.statFor("domino").wins
        "level" -> Profile.level
        else -> 0
    }

    fun isClaimed(id: String): Boolean = id in claimed
    fun isComplete(achievement: Achievement): Boolean = progress(achievement) >= achievement.target

    fun claim(achievement: Achievement): Boolean {
        if (isClaimed(achievement.id) || !isComplete(achievement)) return false
        Profile.addCoins(achievement.reward)
        claimed = claimed + achievement.id
        sp.edit().putStringSet("claimed", claimed).apply()
        return true
    }
}
