package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.fiestadecartas.app.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DailyMission(
    val id: String,
    val titleRes: Int,
    val target: Int,
    val reward: Int,
    val icon: String
)

/**
 * Three small daily goals. Progress is local, resets when the device date changes and
 * rewards are paid in coins once per day.
 */
object DailyMissions {
    private lateinit var sp: SharedPreferences
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private var dayKey: String = ""
    var progress by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var claimed by mutableStateOf<Set<String>>(emptySet())
        private set

    val missions = listOf(
        DailyMission("play2", R.string.mission_play_2, 2, 40, "🎴"),
        DailyMission("win1", R.string.mission_win_1, 1, 60, "🏅"),
        DailyMission("social1", R.string.mission_social_1, 1, 50, "📡")
    )

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_missions", Context.MODE_PRIVATE)
        refreshDay()
    }

    private fun refreshDay() {
        val today = dayFormat.format(Date())
        dayKey = sp.getString("day", "") ?: ""
        if (dayKey != today) {
            dayKey = today
            progress = emptyMap()
            claimed = emptySet()
            sp.edit().putString("day", today).putString("progress", "").putStringSet("claimed", emptySet()).apply()
        } else {
            progress = sp.getString("progress", "")
                ?.split(";")
                ?.mapNotNull {
                    val parts = it.split(":")
                    val id = parts.getOrNull(0) ?: return@mapNotNull null
                    val value = parts.getOrNull(1)?.toIntOrNull() ?: return@mapNotNull null
                    id to value
                }
                ?.toMap()
                ?: emptyMap()
            claimed = sp.getStringSet("claimed", emptySet())?.toSet() ?: emptySet()
        }
    }

    fun progressOf(mission: DailyMission): Int = progress[mission.id] ?: 0
    fun isComplete(mission: DailyMission): Boolean = progressOf(mission) >= mission.target
    fun isClaimed(mission: DailyMission): Boolean = mission.id in claimed

    fun onMatch(gameId: String, won: Boolean, mode: String) {
        refreshDay()
        val next = progress.toMutableMap()
        next["play2"] = (next["play2"] ?: 0) + 1
        if (won) next["win1"] = (next["win1"] ?: 0) + 1
        if (mode != "bots") next["social1"] = (next["social1"] ?: 0) + 1
        progress = next
        persist()
    }

    fun claim(mission: DailyMission): Boolean {
        refreshDay()
        if (isClaimed(mission) || !isComplete(mission)) return false
        Profile.addCoins(mission.reward)
        claimed = claimed + mission.id
        persist()
        return true
    }

    private fun persist() {
        if (!::sp.isInitialized) return
        val encoded = progress.entries.joinToString(";") { "${it.key}:${it.value}" }
        sp.edit().putString("progress", encoded).putStringSet("claimed", claimed).apply()
    }
}
