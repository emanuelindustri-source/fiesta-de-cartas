package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

/**
 * Per-game rules the player can adjust before a match.
 * Everything is stored locally and applies to bot matches immediately.
 * Hosts use the same player count when creating a WiFi room.
 */
object GameConfig {
    private lateinit var sp: SharedPreferences

    var caidaPlayers by mutableIntStateOf(3)
        private set
    var caidaTarget by mutableIntStateOf(24)
        private set
    var fiestaPlayers by mutableIntStateOf(4)
        private set
    var fiestaTarget by mutableIntStateOf(300)
        private set
    var ludoHomeTokens by mutableIntStateOf(4)
        private set
    var nerviosoStartCards by mutableIntStateOf(7)
        private set

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_rules", Context.MODE_PRIVATE)
        caidaPlayers = sp.getInt("caida_players", 3).coerceIn(2, 4)
        caidaTarget = sp.getInt("caida_target", 24).let { if (it in listOf(24, 30, 50)) it else 24 }
        fiestaPlayers = sp.getInt("fiesta_players", 4).coerceIn(2, 4)
        fiestaTarget = sp.getInt("fiesta_target", 300).let { if (it in listOf(200, 300, 500)) it else 300 }
        ludoHomeTokens = sp.getInt("ludo_home", 4).coerceIn(2, 4)
        nerviosoStartCards = sp.getInt("nervioso_cards", 7).coerceIn(3, 9)
    }

    fun updateCaidaPlayers(value: Int) {
        caidaPlayers = value.coerceIn(2, 4)
        sp.edit().putInt("caida_players", caidaPlayers).apply()
    }

    fun updateCaidaTarget(value: Int) {
        if (value !in listOf(24, 30, 50)) return
        caidaTarget = value
        sp.edit().putInt("caida_target", value).apply()
    }

    fun updateFiestaPlayers(value: Int) {
        fiestaPlayers = value.coerceIn(2, 4)
        sp.edit().putInt("fiesta_players", fiestaPlayers).apply()
    }

    fun updateFiestaTarget(value: Int) {
        if (value !in listOf(200, 300, 500)) return
        fiestaTarget = value
        sp.edit().putInt("fiesta_target", value).apply()
    }

    fun updateLudoHomeTokens(value: Int) {
        ludoHomeTokens = value.coerceIn(2, 4)
        sp.edit().putInt("ludo_home", ludoHomeTokens).apply()
    }

    fun updateNerviosoStartCards(value: Int) {
        nerviosoStartCards = value.coerceIn(3, 9)
        sp.edit().putInt("nervioso_cards", nerviosoStartCards).apply()
    }

    fun playersFor(gameId: String): Int = when (gameId) {
        "caida" -> caidaPlayers
        "fiesta" -> fiestaPlayers
        "ludo" -> 4
        "kemix" -> 4
        else -> 4
    }
}
