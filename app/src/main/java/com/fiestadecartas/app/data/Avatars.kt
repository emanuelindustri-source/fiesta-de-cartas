package com.fiestadecartas.app.data

import com.fiestadecartas.app.R

data class Avatar(
    val id: String,
    val nameRes: Int,
    val emoji: String,
    val color: Long,
    val shout: String
)

/**
 * Ten original characters inspired by Venezuelan wildlife and festival culture.
 * They are cosmetic: the player picks one as a profile and sees it react during games.
 */
object Avatars {
    val all = listOf(
        Avatar("turpial", R.string.avatar_turpial, "🐦", 0xFFFFB703, "¡Gol!"),
        Avatar("guacamaya", R.string.avatar_guacamaya, "🦜", 0xFFE63946, "¡Vuela!"),
        Avatar("jaguar", R.string.avatar_jaguar, "🐆", 0xFFC9962B, "¡Ruge!"),
        Avatar("caiman", R.string.avatar_caiman, "🐊", 0xFF2A9D8F, "¡Muerde!"),
        Avatar("delfin", R.string.avatar_delfin, "🐬", 0xFF3A86FF, "¡Salta!"),
        Avatar("tortuga", R.string.avatar_tortuga, "🐢", 0xFF2A6F3E, "¡Aguanta!"),
        Avatar("mono", R.string.avatar_mono, "🐒", 0xFF8E7DBE, "¡Uy!"),
        Avatar("flamenco", R.string.avatar_flamenco, "🦩", 0xFFFF6B9D, "¡Olé!"),
        Avatar("mariposa", R.string.avatar_mariposa, "🦋", 0xFF4CC9F0, "¡Fiu!"),
        Avatar("ardilla", R.string.avatar_ardilla, "🐿️", 0xFFB5651D, "¡Zas!")
    )

    fun byId(id: String): Avatar = all.firstOrNull { it.id == id } ?: all.first()
}
