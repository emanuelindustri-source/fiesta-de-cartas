package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class GameStat(
    val played: Int = 0,
    val wins: Int = 0,
    val bestScore: Int = 0,
    val totalScore: Int = 0
)

@Serializable
data class MatchRecord(
    val id: Long = System.currentTimeMillis(),
    val gameId: String,
    val won: Boolean,
    val score: Int,
    val mode: String,
    val players: Int,
    val seed: Long,
    val playerNames: List<String> = emptyList(),
    val messages: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
private data class PersistedStats(
    val games: Map<String, GameStat> = emptyMap(),
    val recent: List<MatchRecord> = emptyList(),
    val modes: Map<String, Int> = emptyMap()
)

/**
 * Local, private match statistics. It stores per-game totals and the last 20 matches
 * with their seed and narrated history so the player can review or replay them.
 */
object GameStats {
    private lateinit var sp: SharedPreferences
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    var games by mutableStateOf<Map<String, GameStat>>(emptyMap())
        private set
    var recent by mutableStateOf<List<MatchRecord>>(emptyList())
        private set
    var modes by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    val totalPlayed: Int get() = games.values.sumOf { it.played }
    val totalWins: Int get() = games.values.sumOf { it.wins }

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_stats", Context.MODE_PRIVATE)
        load()
    }

    private fun load() {
        val raw = sp.getString("data", null)
        if (raw.isNullOrBlank()) return
        runCatching {
            val data = json.decodeFromString<PersistedStats>(raw)
            games = data.games
            recent = data.recent
            modes = data.modes
        }
    }

    fun statFor(gameId: String): GameStat = games[gameId] ?: GameStat()

    fun recordMatch(
        gameId: String,
        won: Boolean,
        score: Int,
        mode: String,
        players: Int,
        seed: Long,
        playerNames: List<String>,
        messages: List<String>
    ) {
        val previous = statFor(gameId)
        val updated = previous.copy(
            played = previous.played + 1,
            wins = previous.wins + if (won) 1 else 0,
            bestScore = maxOf(previous.bestScore, score),
            totalScore = previous.totalScore + score
        )
        games = games + (gameId to updated)
        modes = modes + (mode to ((modes[mode] ?: 0) + 1))
        val record = MatchRecord(
            gameId = gameId,
            won = won,
            score = score,
            mode = mode,
            players = players,
            seed = seed,
            playerNames = playerNames.take(4),
            messages = messages.takeLast(200)
        )
        recent = (listOf(record) + recent).take(20)
        persist()
    }

    fun clear() {
        games = emptyMap()
        recent = emptyList()
        modes = emptyMap()
        persist()
    }

    private fun persist() {
        if (!::sp.isInitialized) return
        val data = PersistedStats(games, recent, modes)
        sp.edit().putString("data", json.encodeToString(PersistedStats.serializer(), data)).apply()
    }
}
