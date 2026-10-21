package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.pow

/**
 * Local player profile: experience, level (1..100), chosen avatar and virtual chips.
 *
 * The curve is intentionally long: every level asks for more points than the previous
 * one, so reaching level 100 takes months of regular play. Points are earned only by
 * playing (winning gives more) and never expire.
 */
object Profile {
    private lateinit var sp: SharedPreferences

    private const val MAX_LEVEL = 100

    /** Cumulative XP needed to start each level (index 0 = level 1). */
    private val levelStart: IntArray = IntArray(MAX_LEVEL).also { starts ->
        var total = 0
        for (level in 1 until MAX_LEVEL) {
            starts[level - 1] = total
            total += xpStep(level)
        }
        starts[MAX_LEVEL - 1] = total
    }

    /** XP required to go from [level] to level + 1. */
    fun xpStep(level: Int): Int {
        val l = level.coerceIn(1, MAX_LEVEL - 1)
        return (40 + 8 * l + l.toDouble().pow(1.35)).toInt()
    }

    var xp by mutableIntStateOf(0)
        private set
    var avatarId by mutableStateOf("turpial")
        private set
    var chips by mutableIntStateOf(500)
        private set
    var levelUpTo by mutableIntStateOf(0)
        private set

    val level: Int get() = levelFor(xp)
    val levelFloor: Int get() = levelStart[level - 1]
    val levelCeil: Int get() = if (level >= MAX_LEVEL) xp.coerceAtLeast(levelFloor) else levelStart[level]
    val progress: Float
        get() {
            if (level >= MAX_LEVEL) return 1f
            val span = (levelCeil - levelFloor).coerceAtLeast(1)
            return ((xp - levelFloor).toFloat() / span).coerceIn(0f, 1f)
        }
    val xpToNext: Int get() = if (level >= MAX_LEVEL) 0 else (levelCeil - xp).coerceAtLeast(0)
    val avatar: Avatar get() = Avatars.byId(avatarId)

    val rankRes: Int
        get() = when {
            level >= 80 -> com.fiestadecartas.app.R.string.rank_legend
            level >= 55 -> com.fiestadecartas.app.R.string.rank_master
            level >= 35 -> com.fiestadecartas.app.R.string.rank_veteran
            level >= 20 -> com.fiestadecartas.app.R.string.rank_player
            level >= 8 -> com.fiestadecartas.app.R.string.rank_rookie
            else -> com.fiestadecartas.app.R.string.rank_new
        }

    fun levelFor(points: Int): Int {
        var result = 1
        for (i in 0 until MAX_LEVEL) {
            if (points >= levelStart[i]) result = i + 1 else break
        }
        return result
    }

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_profile", Context.MODE_PRIVATE)
        xp = sp.getInt("xp", 0)
        avatarId = sp.getString("avatar", "turpial") ?: "turpial"
        chips = sp.getInt("chips", 500)
    }

    /** Adds points and returns how many levels were gained. */
    fun addXp(amount: Int): Int {
        if (amount <= 0) return 0
        val before = level
        xp = (xp + amount).coerceAtLeast(0)
        sp.edit().putInt("xp", xp).apply()
        val gained = level - before
        if (gained > 0) levelUpTo = level
        return gained
    }

    fun clearLevelUp() {
        levelUpTo = 0
    }

    fun setAvatar(id: String) {
        if (Avatars.byId(id).id != id) return
        avatarId = id
        sp.edit().putString("avatar", id).apply()
    }

    fun addChips(amount: Int) {
        chips = (chips + amount).coerceAtLeast(0)
        sp.edit().putInt("chips", chips).apply()
    }

    fun reset() {
        xp = 0
        chips = 500
        sp.edit().putInt("xp", 0).putInt("chips", 500).apply()
    }
}
