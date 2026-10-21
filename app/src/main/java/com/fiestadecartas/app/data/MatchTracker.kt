package com.fiestadecartas.app.data

/**
 * Single entry point for the end of a match: XP, per-game stats, daily missions and
 * the replay record. Keeping it in one place avoids double-counting.
 */
object MatchTracker {
    fun record(
        gameId: String,
        won: Boolean,
        score: Int,
        mode: String,
        players: Int,
        seed: Long,
        names: List<String>,
        messages: List<String>
    ) {
        Prefs.recordResult(won)
        GameStats.recordMatch(gameId, won, score, mode, players, seed, names, messages)
        DailyMissions.onMatch(gameId, won, mode)
    }
}
