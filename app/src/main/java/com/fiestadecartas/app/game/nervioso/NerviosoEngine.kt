package com.fiestadecartas.app.game.nervioso

import com.fiestadecartas.app.game.SpanishCard
import com.fiestadecartas.app.game.spanishDeck
import kotlin.random.Random

enum class NerviosoPhase { TURN, SLAP, OVER }

data class NerviosoPlay(val match: Boolean, val won: Boolean)

class NerviosoGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis(),
    val startCards: Int = 7
) {
    private val rng = Random(seed)
    private val deck = ArrayDeque<SpanishCard>()
    private val numbers = intArrayOf(1, 2, 3, 4, 5, 6, 7, 10, 11, 12)

    val hands: List<MutableList<SpanishCard>> = List(names.size) { mutableListOf() }
    val pile = mutableListOf<SpanishCard>()
    val slaps = mutableListOf<Int>()

    var current = 0
        private set
    var numberIndex = 0
        private set
    var phase = NerviosoPhase.TURN
        private set
    var message = "Cada jugador descarta y dice un número"
        private set
    var winner = -1
        private set
    var lastCard: SpanishCard? = null
        private set
    var lastSeat = -1
        private set
    var slapRound = 0
        private set
    var turnCount = 0
        private set

    val announced: Int get() = numbers[numberIndex]

    init {
        val cards = spanishDeck().shuffled(rng)
        deck.addAll(cards)
        repeat(startCards) {
            for (seat in names.indices) {
                deck.removeFirstOrNull()?.let { hands[seat].add(it) }
            }
        }
        deck.removeFirstOrNull()?.let { pile.add(it) }
    }

    fun playCard(seat: Int, index: Int): NerviosoPlay? {
        if (phase != NerviosoPhase.TURN || seat != current) return null
        val hand = hands.getOrNull(seat) ?: return null
        if (index !in hand.indices) return null
        val card = hand.removeAt(index)
        lastCard = card
        lastSeat = seat
        pile.add(card)
        turnCount++
        val match = card.rank == announced
        if (hand.isEmpty()) {
            winner = seat
            phase = NerviosoPhase.OVER
            message = "${names[seat]} se quedó sin cartas. ¡Gana la partida!"
            return NerviosoPlay(match, true)
        }
        if (match) {
            phase = NerviosoPhase.SLAP
            slaps.clear()
            slapRound++
            message = "¡${names[seat]} dijo ${announced} y era ${card.rank}! ¡Todos a la pila!"
        } else {
            advanceTurn()
            message = "${names[seat]} dijo ${announced}… no coincide."
        }
        return NerviosoPlay(match, false)
    }

    fun slap(seat: Int): Int {
        if (phase != NerviosoPhase.SLAP) return -1
        if (seat !in names.indices || seat in slaps) return -1
        slaps += seat
        if (slaps.size >= names.size) {
            val loser = slaps.last()
            hands[loser].addAll(pile)
            val taken = pile.size
            pile.clear()
            deck.removeFirstOrNull()?.let { pile.add(it) }
            lastSeat = loser
            current = (loser + 1) % names.size
            numberIndex = 0
            phase = NerviosoPhase.TURN
            message = "${names[loser]} tocó de último y se lleva $taken cartas"
            return loser
        }
        message = "${names[seat]} tocó la pila"
        return -1
    }

    fun botPick(seat: Int): Int {
        val hand = hands.getOrNull(seat).orEmpty()
        if (hand.isEmpty()) return -1
        val match = hand.indexOfFirst { it.rank == announced }
        return if (match >= 0 && rng.nextFloat() < 0.62f) match else rng.nextInt(hand.size)
    }

    fun botSlapDelay(seat: Int): Long {
        val base = 420L + (seat * 137L) % 260L
        return base + rng.nextLong(0L, 220L)
    }

    fun isBotTurn(): Boolean = current in botSeats && phase == NerviosoPhase.TURN

    private fun advanceTurn() {
        current = (current + 1) % names.size
        numberIndex = (numberIndex + 1) % numbers.size
    }
}
