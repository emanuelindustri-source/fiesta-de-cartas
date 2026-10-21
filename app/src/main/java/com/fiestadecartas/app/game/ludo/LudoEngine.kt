package com.fiestadecartas.app.game.ludo

import kotlin.random.Random

data class Cell(val x: Int, val y: Int)

/** 52-cell ring, clockwise. Each arm contributes 13 cells. */
val LUDO_PATH: List<Cell> = listOf(
    Cell(6, 13), Cell(6, 12), Cell(6, 11), Cell(6, 10), Cell(6, 9),
    Cell(5, 8), Cell(4, 8), Cell(3, 8), Cell(2, 8), Cell(1, 8), Cell(0, 8),
    Cell(0, 7),
    Cell(0, 6), Cell(1, 6), Cell(2, 6), Cell(3, 6), Cell(4, 6), Cell(5, 6),
    Cell(6, 5), Cell(6, 4), Cell(6, 3), Cell(6, 2), Cell(6, 1), Cell(6, 0),
    Cell(7, 0),
    Cell(8, 0), Cell(8, 1), Cell(8, 2), Cell(8, 3), Cell(8, 4), Cell(8, 5),
    Cell(9, 6), Cell(10, 6), Cell(11, 6), Cell(12, 6), Cell(13, 6), Cell(14, 6),
    Cell(14, 7),
    Cell(14, 8), Cell(13, 8), Cell(12, 8), Cell(11, 8), Cell(10, 8), Cell(9, 8),
    Cell(8, 9), Cell(8, 10), Cell(8, 11), Cell(8, 12), Cell(8, 13), Cell(8, 14),
    Cell(7, 14),
    Cell(6, 14)
)

val LUDO_STARTS = listOf(0, 13, 26, 39)
val LUDO_SAFE = setOf(0, 8, 13, 21, 26, 34, 39, 47)
val LUDO_LANES: List<List<Cell>> = listOf(
    listOf(Cell(7, 13), Cell(7, 12), Cell(7, 11), Cell(7, 10), Cell(7, 9)),
    listOf(Cell(1, 7), Cell(2, 7), Cell(3, 7), Cell(4, 7), Cell(5, 7)),
    listOf(Cell(7, 1), Cell(7, 2), Cell(7, 3), Cell(7, 4), Cell(7, 5)),
    listOf(Cell(13, 7), Cell(12, 7), Cell(11, 7), Cell(10, 7), Cell(9, 7))
)
val LUDO_YARD_ORIGIN = listOf(Cell(0, 9), Cell(0, 0), Cell(9, 0), Cell(9, 9))
val LUDO_COLORS = listOf(0xFFE63946L, 0xFF2A9D8F, 0xFFF4B400, 0xFF3A86FF)

enum class LudoPhase { PLAYING, OVER }

class LudoGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis(),
    val homeTokens: Int = 4
) {
    private val rng = Random(seed)
    val tokens = Array(4) { IntArray(4) { -1 } }
    var current = 0
        private set
    var dice = 1
        private set
    var rolled = false
        private set
    var phase = LudoPhase.PLAYING
        private set
    var message = "Tira el dado para comenzar"
        private set
    var winner = -1
        private set
    var sixStreak = 0
        private set
    var captures = IntArray(4)
        private set
    var lastMovedToken = -1
        private set
    var lastCapture = false
        private set
    var lastHome = false
        private set

    fun roll(): Boolean {
        if (phase != LudoPhase.PLAYING || rolled) return false
        dice = rng.nextInt(1, 7)
        rolled = true
        lastCapture = false
        lastHome = false
        lastMovedToken = -1
        if (dice == 6) sixStreak += 1 else sixStreak = 0
        message = "${names[current]} sacó $dice"
        if (sixStreak >= 3) {
            message = "${names[current]} sacó tres 6 seguidos: pierde el turno"
            sixStreak = 0
            rolled = false
            nextPlayer()
            return true
        }
        if (legalMoves().isEmpty()) {
            message = "${names[current]} sacó $dice · sin movimientos"
            return true
        }
        return true
    }

    fun legalMoves(): List<Int> {
        if (!rolled) return emptyList()
        return (0..3).filter { canMove(it, dice) }
    }

    fun canMove(token: Int, steps: Int): Boolean {
        val pos = tokens[current][token]
        return if (pos < 0) steps == 6 else pos + steps <= 56
    }

    fun move(token: Int): Boolean {
        if (phase != LudoPhase.PLAYING || !rolled) return false
        if (!canMove(token, dice)) return false
        val player = current
        val old = tokens[player][token]
        val target = if (old < 0) 0 else old + dice
        tokens[player][token] = target
        lastMovedToken = token
        lastHome = target == 56
        lastCapture = false
        if (target in 0..50) {
            val global = (LUDO_STARTS[player] + target) % 52
            if (global !in LUDO_SAFE) {
                for (p in 0 until 4) {
                    if (p == player) continue
                    for (t in 0..3) {
                        val tp = tokens[p][t]
                        if (tp in 0..50 && (LUDO_STARTS[p] + tp) % 52 == global) {
                            tokens[p][t] = -1
                            captures[player] += 1
                            lastCapture = true
                            message = "${names[player]} capturó una ficha de ${names[p]}"
                        }
                    }
                }
            }
        }
        if (lastHome) message = "${names[player]} llevó una ficha a casa"
        if (tokens[player].count { it == 56 } >= homeTokens) {
            winner = player
            phase = LudoPhase.OVER
            message = "¡${names[player]} ganó el Ludo!"
            return true
        }
        rolled = false
        sixStreak = if (dice == 6) sixStreak else 0
        val extra = dice == 6 || lastCapture || lastHome
        if (extra) {
            message += " · turno extra"
        } else {
            sixStreak = 0
            nextPlayer()
        }
        return true
    }

    fun pass(): Boolean {
        if (!rolled || legalMoves().isNotEmpty()) return false
        rolled = false
        sixStreak = 0
        nextPlayer()
        return true
    }

    private fun nextPlayer() {
        if (phase != LudoPhase.PLAYING) return
        current = (current + 1) % 4
        dice = 1
        message = "Turno de ${names[current]}"
    }

    fun botMove(): Boolean {
        if (phase != LudoPhase.PLAYING) return false
        if (!rolled) return roll()
        val moves = legalMoves()
        if (moves.isEmpty()) return pass()
        return move(botPick())
    }

    fun botPick(): Int {
        val moves = legalMoves()
        val chosen = moves.maxByOrNull { t ->
            val pos = tokens[current][t]
            var score = 0
            if (pos < 0) score += 30
            val target = if (pos < 0) 0 else pos + dice
            if (target == 56) score += 100
            if (target in 0..50) {
                val global = (LUDO_STARTS[current] + target) % 52
                if (global !in LUDO_SAFE) {
                    for (p in 0 until 4) {
                        if (p == current) continue
                        for (i in 0..3) {
                            val tp = tokens[p][i]
                            if (tp in 0..50 && (LUDO_STARTS[p] + tp) % 52 == global) score += 80
                        }
                    }
                }
                if (global in LUDO_SAFE) score += 6
            }
            score + target
        } ?: moves.first()
        return chosen
    }

    fun tokenCell(player: Int, token: Int): Cell? {
        val pos = tokens[player][token]
        return when {
            pos < 0 -> null
            pos <= 50 -> LUDO_PATH[(LUDO_STARTS[player] + pos) % 52]
            pos <= 55 -> LUDO_LANES[player][pos - 51]
            else -> null
        }
    }

    fun yardCell(player: Int, token: Int): Cell {
        val o = LUDO_YARD_ORIGIN[player]
        val dx = listOf(1, 4, 1, 4)[token]
        val dy = listOf(1, 1, 4, 4)[token]
        return Cell(o.x + dx, o.y + dy)
    }

    fun homeCount(player: Int): Int = tokens[player].count { it == 56 }
    fun isBotTurn(): Boolean = current in botSeats && phase == LudoPhase.PLAYING
}
