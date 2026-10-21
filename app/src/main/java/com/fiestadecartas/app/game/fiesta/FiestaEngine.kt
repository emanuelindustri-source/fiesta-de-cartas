package com.fiestadecartas.app.game.fiesta

import com.fiestadecartas.app.game.FiestaCard
import com.fiestadecartas.app.game.FiestaColor
import com.fiestadecartas.app.game.FiestaKind
import com.fiestadecartas.app.game.fiestaDeck
import kotlin.random.Random

enum class FiestaPhase { PLAYING, ROUND_OVER, OVER }

class FiestaGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis(),
    val target: Int = 300
) {
    private val rng = Random(seed)
    val n = names.size
    val hands = Array(n) { mutableListOf<FiestaCard>() }
    var deck = mutableListOf<FiestaCard>()
        private set
    var discard = mutableListOf<FiestaCard>()
        private set
    var current = 0
        private set
    var direction = 1
        private set
    var color = FiestaColor.RED
        private set
    var hasDrawn = false
        private set
    var scores = IntArray(n)
        private set
    var phase = FiestaPhase.PLAYING
        private set
    var winner = -1
        private set
    var message = ""
        private set
    var round = 1
        private set
    var log = mutableListOf<String>()
        private set

    init { newRound(first = true) }

    private fun newRound(first: Boolean) {
        hands.forEach { it.clear() }
        deck = fiestaDeck().shuffled(rng).toMutableList()
        discard = mutableListOf()
        repeat(7) { repeat(n) { if (deck.isNotEmpty()) hands[it] += deck.removeLast() } }
        var top = deck.removeLast()
        while (top.color == FiestaColor.WILD && deck.isNotEmpty()) {
            deck.add(0, top)
            top = deck.removeLast()
        }
        discard += top
        color = if (top.color == FiestaColor.WILD) FiestaColor.RED else top.color
        current = if (first) 0 else (winner.coerceAtLeast(0) + 1) % n
        direction = 1
        hasDrawn = false
        winner = -1
        phase = FiestaPhase.PLAYING
        log = mutableListOf()
        message = "Comienza ${names[current]}"
        if (!first) round += 1
    }

    fun top(): FiestaCard = discard.last()

    fun playable(card: FiestaCard): Boolean {
        if (card.color == FiestaColor.WILD) return true
        val t = top()
        return card.color == color || (t.kind == FiestaKind.NUMBER && card.kind == FiestaKind.NUMBER && card.number == t.number) ||
            (t.kind != FiestaKind.NUMBER && card.kind == t.kind)
    }

    fun play(cardId: Int, chosenColor: FiestaColor? = null): Boolean {
        if (phase != FiestaPhase.PLAYING) return false
        val hand = hands[current]
        val idx = hand.indexOfFirst { it.id == cardId }
        if (idx < 0) return false
        val card = hand[idx]
        if (!playable(card)) return false
        val player = current
        hand.removeAt(idx)
        discard += card
        if (card.color == FiestaColor.WILD) color = chosenColor ?: mostCommonColor(hand)
        else color = card.color
        log.add("${names[player]}: ${describe(card)}")
        if (hand.isEmpty()) {
            val gained = hands.indices.filter { it != player }.sumOf { p -> hands[p].sumOf { it.points } }
            scores[player] += gained
            winner = player
            phase = if (scores[player] >= target) FiestaPhase.OVER else FiestaPhase.ROUND_OVER
            message = "¡${names[player]} se quedó sin cartas! +$gained puntos"
            return true
        }
        when (card.kind) {
            FiestaKind.SKIP -> {
                message = "${names[player]} saltó a ${names[(player + direction + n) % n]}"
                advance(2)
            }
            FiestaKind.REVERSE -> {
                direction *= -1
                message = "${names[player]} invirtió el sentido"
                if (n == 2) advance(2) else advance(1)
            }
            FiestaKind.DRAW2 -> {
                val victim = (player + direction + n) % n
                repeat(2) { drawTo(victim) }
                message = "${names[player]} hizo robar 2 a ${names[victim]}"
                advance(2)
            }
            FiestaKind.WILD4 -> {
                val victim = (player + direction + n) % n
                repeat(4) { drawTo(victim) }
                message = "${names[player]} eligió $color y ${names[victim]} roba 4"
                advance(2)
            }
            else -> {
                message = "${names[player]} jugó ${describe(card)}"
                advance(1)
            }
        }
        hasDrawn = false
        return true
    }

    fun drawAndMaybePass(): Boolean {
        if (phase != FiestaPhase.PLAYING || hasDrawn) return false
        val player = current
        val card = drawTo(player)
        hasDrawn = true
        if (card == null) {
            message = "No quedan cartas · ${names[player]} pasa"
            advance(1); hasDrawn = false
            return true
        }
        message = "${names[player]} robó una carta"
        if (!playable(card)) {
            message = "${names[player]} robó y pasa"
            advance(1); hasDrawn = false
        }
        return true
    }

    fun passTurn(): Boolean {
        if (phase != FiestaPhase.PLAYING || !hasDrawn) return false
        advance(1)
        hasDrawn = false
        return true
    }

    fun nextRound() {
        if (phase != FiestaPhase.ROUND_OVER) return
        newRound(first = false)
    }

    private fun advance(steps: Int) {
        repeat(steps) { current = ((current + direction) % n + n) % n }
    }

    private fun drawTo(player: Int): FiestaCard? {
        if (deck.isEmpty()) {
            if (discard.size <= 1) return null
            val topCard = discard.removeAt(discard.size - 1)
            val rest = discard.toMutableList()
            discard = mutableListOf(topCard)
            deck = rest.shuffled(rng).toMutableList()
        }
        val card = if (deck.isEmpty()) null else deck.removeLast()
        if (card != null) hands[player] += card
        return card
    }

    private fun mostCommonColor(hand: List<FiestaCard>): FiestaColor {
        val counts = hand.filter { it.color != FiestaColor.WILD }.groupingBy { it.color }.eachCount()
        return counts.maxByOrNull { it.value }?.key ?: FiestaColor.RED
    }

    private fun describe(card: FiestaCard): String = when (card.kind) {
        FiestaKind.NUMBER -> "${card.number} ${card.color.key}"
        FiestaKind.SKIP -> "salta ${card.color.key}"
        FiestaKind.REVERSE -> "reversa ${card.color.key}"
        FiestaKind.DRAW2 -> "+2 ${card.color.key}"
        FiestaKind.WILD -> "comodín"
        FiestaKind.WILD4 -> "+4 comodín"
    }

    fun botMove(): Boolean {
        if (phase != FiestaPhase.PLAYING || current !in botSeats) return false
        val hand = hands[current]
        val playableCards = hand.filter { playable(it) }
        if (playableCards.isEmpty()) {
            return if (!hasDrawn) drawAndMaybePass() else passTurn()
        }
        val chosen = playableCards.maxByOrNull { c ->
            when {
                c.kind == FiestaKind.WILD4 -> 30
                c.kind == FiestaKind.WILD -> 25
                c.kind == FiestaKind.SKIP || c.kind == FiestaKind.DRAW2 -> 40
                c.kind == FiestaKind.REVERSE -> 20
                c.color == color -> 10 + c.number
                else -> c.number
            }
        } ?: playableCards.first()
        val chosenColor = if (chosen.color == FiestaColor.WILD) mostCommonColor(hand) else null
        return play(chosen.id, chosenColor)
    }

    /** Executes one bot turn and returns the wire action for LAN sync. */
    fun botStep(): String? {
        if (phase != FiestaPhase.PLAYING || current !in botSeats) return null
        val hand = hands[current]
        var playableCards = hand.filter { playable(it) }
        if (hasDrawn && playableCards.isEmpty()) {
            passTurn()
            return "S"
        }
        if (playableCards.isEmpty()) {
            drawAndMaybePass()
            return "D"
        }
        val chosen = playableCards.maxByOrNull { c ->
            when {
                c.kind == FiestaKind.WILD4 -> 30
                c.kind == FiestaKind.WILD -> 25
                c.kind == FiestaKind.SKIP || c.kind == FiestaKind.DRAW2 -> 40
                c.kind == FiestaKind.REVERSE -> 20
                c.color == color -> 10 + c.number
                else -> c.number
            }
        } ?: playableCards.first()
        val chosenColor = if (chosen.color == FiestaColor.WILD) mostCommonColor(hand) else null
        play(chosen.id, chosenColor)
        return "P|${chosen.id}|${(chosenColor ?: FiestaColor.RED).ordinal}"
    }

    fun isBotTurn(): Boolean = current in botSeats && phase == FiestaPhase.PLAYING
}
