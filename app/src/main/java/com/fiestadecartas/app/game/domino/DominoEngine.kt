package com.fiestadecartas.app.game.domino

import com.fiestadecartas.app.data.BotDifficulty
import kotlin.random.Random

data class DominoTile(val a: Int, val b: Int) {
    val id: Int get() = minOf(a, b) * 7 + maxOf(a, b)
    val pips: Int get() = a + b
    val isDouble: Boolean get() = a == b
}

data class PlayedTile(val tile: DominoTile, val left: Int, val right: Int)

enum class DominoPhase { PLAYING, ROUND_OVER, OVER }

class DominoGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis(),
    val target: Int = 100
) {
    private val rng = Random(seed)
    val n = names.size
    val hands = Array(n) { mutableListOf<DominoTile>() }
    var boneyard = mutableListOf<DominoTile>()
        private set
    var chain = mutableListOf<PlayedTile>()
        private set
    var leftEnd = -1
        private set
    var rightEnd = -1
        private set
    var points = IntArray(n)
        private set
    var current = 0
        private set
    var phase = DominoPhase.PLAYING
        private set
    var winner = -1
        private set
    var message = ""
        private set
    var log = mutableListOf<String>()
        private set
    var round = 1
        private set
    var dealer = 0
        private set
    var lastPlayed: DominoTile? = null
        private set
    var lastPlayedSeat = -1
        private set
    var lastSide = ""
        private set
    var eventTick = 0
        private set
    private var passes = 0

    init { dealRound() }

    fun dealRound() {
        hands.forEach { it.clear() }
        chain = mutableListOf()
        boneyard = mutableListOf()
        points = points.copyOf()
        passes = 0
        leftEnd = -1
        rightEnd = -1
        winner = -1
        lastPlayed = null
        lastPlayedSeat = -1
        lastSide = ""
        log = mutableListOf()
        val deck = (0..6).flatMap { a -> (a..6).map { b -> DominoTile(a, b) } }.shuffled(rng).toMutableList()
        repeat(7) { for (seat in 0 until n) if (deck.isNotEmpty()) hands[seat] += deck.removeLast() }
        boneyard = deck
        current = (dealer + 1) % n
        phase = DominoPhase.PLAYING
        message = "Turno de ${names[current]}"
        eventTick++
    }

    fun canPlay(tile: DominoTile): Boolean = when {
        chain.isEmpty() -> true
        else -> tile.a == leftEnd || tile.b == leftEnd || tile.a == rightEnd || tile.b == rightEnd
    }

    fun canPlayLeft(tile: DominoTile): Boolean = chain.isEmpty() || tile.a == leftEnd || tile.b == leftEnd
    fun canPlayRight(tile: DominoTile): Boolean = chain.isEmpty() || tile.a == rightEnd || tile.b == rightEnd

    fun playableIndices(seat: Int = current): List<Int> =
        hands.getOrElse(seat) { emptyList() }.indices.filter { canPlay(hands[seat][it]) }

    fun play(index: Int, side: String): Boolean {
        if (phase != DominoPhase.PLAYING) return false
        val hand = hands[current]
        if (index !in hand.indices) return false
        val tile = hand[index]
        if (!canPlay(tile)) return false
        if (chain.isNotEmpty() && side == "L" && !canPlayLeft(tile)) return false
        if (chain.isNotEmpty() && side == "R" && !canPlayRight(tile)) return false
        val player = current
        hand.removeAt(index)
        when {
            chain.isEmpty() -> {
                chain += PlayedTile(tile, tile.a, tile.b)
                leftEnd = tile.a
                rightEnd = tile.b
            }
            side == "L" -> {
                val outer = if (tile.a == leftEnd) tile.b else tile.a
                chain.add(0, PlayedTile(tile, outer, leftEnd))
                leftEnd = outer
            }
            else -> {
                val outer = if (tile.a == rightEnd) tile.b else tile.a
                chain.add(PlayedTile(tile, rightEnd, outer))
                rightEnd = outer
            }
        }
        lastPlayed = tile
        lastPlayedSeat = player
        lastSide = if (chain.size == 1) "B" else side
        passes = 0
        eventTick++
        log.add("${names[player]}: ${tile.a}-${tile.b}")
        if (hand.isEmpty()) {
            finishRound(player)
            return true
        }
        message = "${names[player]} jugó ${tile.a}-${tile.b}"
        current = (current + 1) % n
        return true
    }

    fun draw(): Boolean {
        if (phase != DominoPhase.PLAYING) return false
        if (boneyard.isEmpty()) return false
        val tile = boneyard.removeLast()
        hands[current] += tile
        message = "${names[current]} robó una ficha"
        eventTick++
        return true
    }

    fun pass(): Boolean {
        if (phase != DominoPhase.PLAYING) return false
        if (playableIndices().isNotEmpty()) return false
        passes++
        log.add("${names[current]}: pasa")
        message = "${names[current]} pasa"
        if (passes >= n) {
            finishBlocked()
        } else {
            current = (current + 1) % n
            eventTick++
        }
        return true
    }

    private fun finishRound(player: Int) {
        winner = player
        val gained = hands.indices.filter { it != player }.sumOf { p -> hands[p].sumOf { it.pips } }
        points[player] += gained
        phase = if (points[player] >= target) DominoPhase.OVER else DominoPhase.ROUND_OVER
        message = if (phase == DominoPhase.OVER) "¡${names[player]} ganó la partida con ${points[player]} puntos!"
        else "¡Dominó de ${names[player]}! +$gained puntos"
        log.add(message)
        eventTick++
    }

    private fun finishBlocked() {
        val lowest = hands.indices.minByOrNull { hands[it].sumOf { tile -> tile.pips } } ?: 0
        winner = lowest
        val gained = hands.indices.filter { it != lowest }
            .sumOf { p -> hands[p].sumOf { it.pips } } - hands[lowest].sumOf { it.pips }
        points[lowest] += gained.coerceAtLeast(0)
        phase = if (points[lowest] >= target) DominoPhase.OVER else DominoPhase.ROUND_OVER
        message = "Mesa cerrada · gana ${names[lowest]} (+${gained.coerceAtLeast(0)})"
        log.add(message)
        eventTick++
    }

    fun nextRound() {
        if (phase != DominoPhase.ROUND_OVER) return
        dealer = (dealer + 1) % n
        round++
        dealRound()
    }

    fun isBotTurn(): Boolean = current in botSeats && phase == DominoPhase.PLAYING

    /** Returns "P:index:L/R", "D" or "S" for the screen animation loop. */
    fun botMove(difficulty: BotDifficulty = BotDifficulty.BALANCED): String? {
        if (!isBotTurn()) return null
        val playable = playableIndices()
        if (playable.isEmpty()) {
            if (boneyard.isNotEmpty()) {
                draw()
                return "D"
            }
            pass()
            return "S"
        }
        val choice = if (difficulty == BotDifficulty.RELAXED) playable.random(rng)
        else playable.maxByOrNull { hands[current][it].pips } ?: playable.first()
        val tile = hands[current][choice]
        val side = when {
            chain.isEmpty() -> "R"
            canPlayLeft(tile) && canPlayRight(tile) -> if (difficulty == BotDifficulty.SHARP) {
                if (leftEnd <= rightEnd) "L" else "R"
            } else if (rng.nextBoolean()) "L" else "R"
            canPlayLeft(tile) -> "L"
            else -> "R"
        }
        play(choice, side)
        return "P:$choice:$side"
    }
}
