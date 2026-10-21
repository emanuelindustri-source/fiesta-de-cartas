package com.fiestadecartas.app.game

import androidx.compose.ui.graphics.Color

enum class SpanishSuit(val key: String, val color: Color, val light: Color) {
    OROS("oros", Color(0xFFC9962B), Color(0xFFFFE8A3)),
    COPAS("copas", Color(0xFFC1121F), Color(0xFFFFC2C8)),
    ESPADAS("espadas", Color(0xFF1D3557), Color(0xFFB8D4F0)),
    BASTOS("bastos", Color(0xFF2A6F3E), Color(0xFFBDE8C4))
}

data class SpanishCard(val rank: Int, val suit: SpanishSuit) {
    val id: Int get() = rank * 4 + suit.ordinal
    val name: String get() = when (rank) {
        1 -> "As"; 10 -> "Sota"; 11 -> "Caballo"; 12 -> "Rey"; else -> rank.toString()
    }
    val points: Int get() = when (rank) {
        10 -> 2; 11 -> 3; 12 -> 4; else -> 1
    }
}

val SPANISH_RANKS = listOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)

fun spanishDeck(): List<SpanishCard> =
    SPANISH_RANKS.flatMap { r -> SpanishSuit.entries.map { s -> SpanishCard(r, s) } }

enum class FiestaColor(val key: String) {
    RED("red"), YELLOW("yellow"), GREEN("green"), BLUE("blue"), WILD("wild")
}

enum class FiestaKind { NUMBER, SKIP, REVERSE, DRAW2, WILD, WILD4 }

data class FiestaCard(
    val id: Int,
    val color: FiestaColor,
    val kind: FiestaKind,
    val number: Int = -1
) {
    val points: Int get() = when (kind) {
        FiestaKind.NUMBER -> number
        FiestaKind.SKIP, FiestaKind.REVERSE, FiestaKind.DRAW2 -> 20
        FiestaKind.WILD, FiestaKind.WILD4 -> 50
    }
    val playableColor: FiestaColor get() = if (color == FiestaColor.WILD) FiestaColor.WILD else color
}

fun fiestaDeck(): List<FiestaCard> {
    var id = 0
    val list = mutableListOf<FiestaCard>()
    val colors = listOf(FiestaColor.RED, FiestaColor.YELLOW, FiestaColor.GREEN, FiestaColor.BLUE)
    colors.forEach { c ->
        list += FiestaCard(id++, c, FiestaKind.NUMBER, 0)
        for (n in 1..9) {
            repeat(2) { list += FiestaCard(id++, c, FiestaKind.NUMBER, n) }
        }
        repeat(2) {
            list += FiestaCard(id++, c, FiestaKind.SKIP)
            list += FiestaCard(id++, c, FiestaKind.REVERSE)
            list += FiestaCard(id++, c, FiestaKind.DRAW2)
        }
    }
    repeat(4) {
        list += FiestaCard(id++, FiestaColor.WILD, FiestaKind.WILD)
        list += FiestaCard(id++, FiestaColor.WILD, FiestaKind.WILD4)
    }
    return list
}

data class KemixCard(val id: Int, val family: Int)

val KEMIX_FAMILIES = listOf("sol", "luna", "estrella", "flor", "turpial", "ola")
val KEMIX_EMOJI = listOf("☀️", "🌙", "⭐", "🌸", "🐦", "🌊")
val KEMIX_COLORS = listOf(
    Color(0xFFFFB703), Color(0xFF8E7DBE), Color(0xFF4CC9F0),
    Color(0xFFFF6B9D), Color(0xFFFF7A00), Color(0xFF2EC4B6)
)

fun kemixDeck(): List<KemixCard> {
    var id = 0
    return (0 until 6).flatMap { fam -> List(4) { KemixCard(id++, fam) } }.shuffled()
}

fun englishDeck(): List<Pair<Int, Int>> = (1..13).flatMap { r -> (0..3).map { r to it } }.shuffled()
