package com.fiestadecartas.app.game.caida

import com.fiestadecartas.app.data.BotDifficulty
import com.fiestadecartas.app.game.SpanishCard
import com.fiestadecartas.app.game.spanishDeck
import kotlin.random.Random

enum class CaidaPhase { PLAYING, ROUND_OVER, OVER }

class CaidaGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis(),
    val mySeat: Int = 0,
    val target: Int = 24
) {
    val n = names.size
    private val rng = Random(seed)
    var deck = mutableListOf<SpanishCard>()
        private set
    val hands = Array(n) { mutableListOf<SpanishCard>() }
    var table = mutableListOf<SpanishCard>()
        private set
    var points = IntArray(n)
        private set
    var captured = IntArray(n)
        private set
    var roundPoints = IntArray(n)
        private set
    var current = 0
        private set
    var dealer = 0
        private set
    var phase = CaidaPhase.PLAYING
        private set
    var winner = -1
        private set
    var message = ""
        private set
    var log = mutableListOf<String>()
        private set
    var round = 1
        private set
    var lastCanto = ""
        private set
    var lastCaptureBy = -1
        private set
    var animatingCapture = false
        private set
    var lastPlayed: SpanishCard? = null
        private set
    var lastPlayedSeat = -1
        private set
    var lastCantoSeat = -1
        private set
    var lastCantoLabel = ""
        private set
    var eventTick = 0
        private set
    var dealTick = 0
        private set

    init { dealNewRound() }

    fun dealNewRound() {
        roundPoints = IntArray(n)
        captured = IntArray(n)
        table = mutableListOf()
        hands.forEach { it.clear() }
        deck = spanishDeck().shuffled(rng).toMutableList()
        repeat(3) { repeat(n) { hands[it] += deck.removeLast() } }
        repeat(4) { if (deck.isNotEmpty()) table += deck.removeLast() }
        phase = CaidaPhase.PLAYING
        winner = -1
        lastCaptureBy = -1
        lastPlayed = null
        lastPlayedSeat = -1
        lastCantoSeat = -1
        lastCantoLabel = ""
        dealTick++
        log = mutableListOf()
        current = (dealer + 1) % n
        // Cantos are declared at the deal, as in the traditional game.
        var cantoText = ""
        for (i in 0 until n) {
            val canto = bestCanto(hands[i]) ?: continue
            points[i] += canto.second
            val label = "${names[i]}: ${canto.first} (+${canto.second})"
            log.add(label)
            lastCantoSeat = i
            lastCantoLabel = canto.first
            cantoText = if (cantoText.isEmpty()) label else cantoText
            if (canto.first == "Trivilín de Rey") {
                winner = i; phase = CaidaPhase.OVER
                message = "${names[i]} ganó con Trivilín de Rey"
                return
            }
        }
        checkWinner()
        message = if (cantoText.isNotEmpty()) cantoText else "Nueva ronda · juega ${names[current]}"
        eventTick++
    }

    fun bestCanto(hand: List<SpanishCard>): Pair<String, Int>? {
        if (hand.size < 3) return null
        val ranks = hand.map { it.rank }.sorted()
        val groups = ranks.groupingBy { it }.eachCount()
        val triple = groups.entries.firstOrNull { it.value >= 3 }?.key
        val pair = groups.entries.filter { it.value >= 2 }.maxByOrNull { it.key }?.key
        fun has(vararg r: Int): Boolean {
            val copy = ranks.toMutableList()
            return r.all { copy.remove(it) }
        }
        fun value(rank: Int) = when (rank) { 10 -> 2; 11 -> 3; 12 -> 4; else -> 1 }
        return when {
            has(1, 12, 12) -> "Casa Grande" to 12
            has(1, 11, 11) -> "Casa Chica" to 11
            has(1, 10, 11) -> "Registríco" to 10
            has(1, 10, 12) -> "Maguaro" to 9
            has(1, 11, 12) -> "Registro" to 8
            pair != null && ranks.any { kotlin.math.abs(it - pair) == 1 && it != pair } -> "Vigía" to 7
            isPatrulla(hand) -> "Patrulla" to 6
            triple != null -> {
                if (triple == 12) "Trivilín de Rey" to 0 else "Trivilín" to 5
            }
            pair != null -> "Ronda" to value(pair)
            else -> null
        }
    }

    private fun isPatrulla(hand: List<SpanishCard>): Boolean {
        if (hand.map { it.suit }.distinct().size != 1) return false
        val r = hand.map { it.rank }.sorted()
        val order = listOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)
        val idx = r.map { order.indexOf(it) }
        return idx.all { it >= 0 } && idx[1] == idx[0] + 1 && idx[2] == idx[0] + 2
    }

    fun playableIndexForCapture(): Int {
        if (table.isEmpty()) return -1
        val top = table.last().rank
        return hands[current].indexOfFirst { it.rank == top }
    }

    fun playCard(index: Int): Boolean {
        if (phase != CaidaPhase.PLAYING) return false
        if (index !in hands[current].indices) return false
        val player = current
        val card = hands[player].removeAt(index)
        lastPlayed = card
        lastPlayedSeat = player
        eventTick++
        val topRank = table.lastOrNull()?.rank
        if (table.isNotEmpty() && card.rank == topRank) {
            val won = table.toMutableList().also { it += card }
            captured[player] += won.size
            val gain = card.points
            points[player] += gain
            lastCaptureBy = player
            animatingCapture = true
            log.add("${names[player]}: ¡CAÍDA! +$gain")
            table = mutableListOf()
            if (deck.isNotEmpty()) {
                // mesa limpia bonus
                points[player] += 4
                log.add("${names[player]}: mesa limpia +4")
            }
            message = "¡Caída de ${names[player]}! +$gain"
        } else {
            table += card
            message = "${names[player]} jugó ${card.name} de ${card.suit.key}"
        }
        if (deck.isNotEmpty()) hands[player] += deck.removeLast()
        if (points[player] >= target) {
            winner = player
            phase = CaidaPhase.OVER
            message = "¡${names[player]} llegó a $target puntos!"
            return true
        }
        current = (current + 1) % n
        checkRoundEnd()
        return true
    }

    fun nextRound() {
        if (phase != CaidaPhase.ROUND_OVER) return
        dealer = (dealer + 1) % n
        round += 1
        dealNewRound()
    }

    private fun checkWinner() {
        val max = points.maxOrNull() ?: 0
        if (max >= target) {
            winner = points.indices.maxByOrNull { points[it] } ?: 0
            phase = CaidaPhase.OVER
        }
    }

    private fun checkRoundEnd() {
        if (hands.all { it.isEmpty() } && deck.isEmpty()) {
            val threshold = when (n) { 2 -> 20; 3 -> 12; else -> 8 }
            for (i in 0 until n) {
                roundPoints[i] = (captured[i] - threshold).coerceAtLeast(0)
                points[i] += roundPoints[i]
            }
            phase = CaidaPhase.ROUND_OVER
            checkWinner()
            if (phase != CaidaPhase.OVER) {
                val best = points.indices.maxByOrNull { points[it] } ?: 0
                message = "Ronda ${round} terminada · lidera ${names[best]} (${points[best]})"
            } else {
                message = "¡${names[winner]} ganó la partida con ${points[winner]} puntos!"
            }
            log.add(message)
        }
    }

    fun botMove(difficulty: BotDifficulty = BotDifficulty.BALANCED): Int {
        if (phase != CaidaPhase.PLAYING) return -1
        val hand = hands[current]
        if (hand.isEmpty()) return -1
        if (difficulty == BotDifficulty.RELAXED) {
            val capture = playableIndexForCapture()
            if (capture >= 0 && rng.nextFloat() < 0.55f) return capture
            return rng.nextInt(hand.size)
        }
        val capture = bestCaptureIndex()
        if (capture >= 0) return capture
        // Avoid feeding a card that the next player can capture when possible.
        val nextSafe = hands[(current + 1) % n].map { it.rank }.toSet()
        val candidates = hand.indices.filter { hand[it].rank !in nextSafe }
        val pool = if (candidates.isNotEmpty()) candidates else hand.indices
        return if (difficulty == BotDifficulty.SHARP) {
            pool.minByOrNull { hand[it].points } ?: 0
        } else {
            pool.minByOrNull { hand[it].points } ?: 0
        }
    }

    private fun bestCaptureIndex(): Int {
        if (table.isEmpty()) return -1
        val top = table.last().rank
        return hands[current].indices
            .filter { hands[current][it].rank == top }
            .maxByOrNull { hands[current][it].points }
            ?: -1
    }

    fun isBotTurn(): Boolean = current in botSeats && phase == CaidaPhase.PLAYING

    fun finishCaptureAnimation() { animatingCapture = false }

    fun scoreFor(player: Int): Int = points[player]
}
