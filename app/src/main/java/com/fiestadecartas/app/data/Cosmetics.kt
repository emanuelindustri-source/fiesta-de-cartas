package com.fiestadecartas.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.fiestadecartas.app.R

data class CardBackStyle(
    val id: String,
    val nameRes: Int,
    val price: Int,
    val navy: Long,
    val navy2: Long,
    val accent: Long
)

data class TableFelt(
    val id: String,
    val nameRes: Int,
    val price: Int,
    val color: Long,
    val accent: Long
)

/**
 * Cosmetic shop. Everything is bought with coins earned by playing; there are no
 * real-money purchases and no accounts.
 */
object Cosmetics {
    private lateinit var sp: SharedPreferences
    var coinsVersion by mutableStateOf(0)
        private set

    val cardBacks = listOf(
        CardBackStyle("classic", R.string.back_classic, 0, 0xFF101E33, 0xFF1D3557, 0xFFD8A93B),
        CardBackStyle("halloween", R.string.back_halloween, 180, 0xFF160B24, 0xFF3A175C, 0xFFFF7A00),
        CardBackStyle("navidad", R.string.back_navidad, 180, 0xFF07231A, 0xFF14533A, 0xFFE63946),
        CardBackStyle("oro", R.string.back_oro, 260, 0xFF3A2410, 0xFF6B4A1F, 0xFFFFD166),
        CardBackStyle("fiesta", R.string.back_fiesta, 220, 0xFF0B2E33, 0xFF126E75, 0xFF2EC4B6)
    )

    val felts = listOf(
        TableFelt("verde", R.string.felt_green, 0, 0xFF0E5A3A, 0xFFD8A93B),
        TableFelt("noche", R.string.felt_night, 160, 0xFF111E2E, 0xFF3A86FF),
        TableFelt("halloween", R.string.felt_halloween, 220, 0xFF2A1038, 0xFFFF7A00),
        TableFelt("rubi", R.string.felt_ruby, 240, 0xFF4A0D1A, 0xFFFF8FA3)
    )

    var selectedCardBack by mutableStateOf("classic")
        private set
    var selectedFelt by mutableStateOf("verde")
        private set
    var ownedCardBacks by mutableStateOf<Set<String>>(emptySet())
        private set
    var ownedFelts by mutableStateOf<Set<String>>(emptySet())
        private set
    var ownedAvatars by mutableStateOf<Set<String>>(emptySet())
        private set

    val cardBack: CardBackStyle
        get() = cardBacks.firstOrNull { it.id == selectedCardBack } ?: cardBacks.first()
    val felt: TableFelt
        get() = felts.firstOrNull { it.id == selectedFelt } ?: felts.first()

    fun init(context: Context) {
        if (::sp.isInitialized) return
        sp = context.getSharedPreferences("fiesta_cosmetics", Context.MODE_PRIVATE)
        val freeBacks = cardBacks.filter { it.price == 0 }.map { it.id }.toSet()
        val freeFelts = felts.filter { it.price == 0 }.map { it.id }.toSet()
        val freeAvatars = Avatars.all.filter { it.price == 0 }.map { it.id }.toSet()
        ownedCardBacks = (sp.getStringSet("backs", freeBacks)?.toSet() ?: freeBacks) + freeBacks
        ownedFelts = (sp.getStringSet("felts", freeFelts)?.toSet() ?: freeFelts) + freeFelts
        ownedAvatars = (sp.getStringSet("avatars", freeAvatars)?.toSet() ?: freeAvatars) +
            freeAvatars + Profile.avatarId
        selectedCardBack = sp.getString("back", "classic")?.takeIf { it in ownedCardBacks } ?: "classic"
        selectedFelt = sp.getString("felt", "verde")?.takeIf { it in ownedFelts } ?: "verde"
    }

    fun isCardBackOwned(id: String) = id in ownedCardBacks
    fun isFeltOwned(id: String) = id in ownedFelts
    fun isAvatarOwned(id: String) = id in ownedAvatars

    fun selectCardBack(style: CardBackStyle) {
        if (!isCardBackOwned(style.id)) return
        selectedCardBack = style.id
        sp.edit().putString("back", style.id).apply()
    }

    fun selectFelt(felt: TableFelt) {
        if (!isFeltOwned(felt.id)) return
        selectedFelt = felt.id
        sp.edit().putString("felt", felt.id).apply()
    }

    fun buyCardBack(style: CardBackStyle): Boolean {
        if (isCardBackOwned(style.id)) return true
        if (!Profile.spendCoins(style.price)) return false
        ownedCardBacks = ownedCardBacks + style.id
        sp.edit().putStringSet("backs", ownedCardBacks).apply()
        selectCardBack(style)
        coinsVersion++
        return true
    }

    fun buyFelt(felt: TableFelt): Boolean {
        if (isFeltOwned(felt.id)) return true
        if (!Profile.spendCoins(felt.price)) return false
        ownedFelts = ownedFelts + felt.id
        sp.edit().putStringSet("felts", ownedFelts).apply()
        selectFelt(felt)
        coinsVersion++
        return true
    }

    fun buyAvatar(avatar: Avatar): Boolean {
        if (isAvatarOwned(avatar.id)) {
            Profile.setAvatar(avatar.id)
            return true
        }
        if (!Profile.spendCoins(avatar.price)) return false
        ownedAvatars = ownedAvatars + avatar.id
        sp.edit().putStringSet("avatars", ownedAvatars).apply()
        Profile.setAvatar(avatar.id)
        coinsVersion++
        return true
    }

    fun colorOf(style: CardBackStyle): Color = Color(style.navy)
}
