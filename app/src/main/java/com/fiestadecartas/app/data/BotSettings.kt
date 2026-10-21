package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class BotDifficulty(val key: String) {
    RELAXED("easy"),
    BALANCED("normal"),
    SHARP("hard");

    companion object {
        fun fromKey(key: String?): BotDifficulty = entries.firstOrNull { it.key == key } ?: BALANCED
    }
}

/** Difficulty shared by all games; Normal is the default. */
object BotSettings {
    private lateinit var sp: SharedPreferences
    var difficulty by mutableStateOf(BotDifficulty.BALANCED)
        private set

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_bots", Context.MODE_PRIVATE)
        difficulty = BotDifficulty.fromKey(sp.getString("difficulty", "normal"))
    }

    fun set(value: BotDifficulty) {
        difficulty = value
        if (::sp.isInitialized) sp.edit().putString("difficulty", value.key).apply()
    }
}
