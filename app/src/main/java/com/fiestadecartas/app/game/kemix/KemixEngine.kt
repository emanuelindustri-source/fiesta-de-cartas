package com.fiestadecartas.app.game.kemix

import com.fiestadecartas.app.game.KemixCard
import com.fiestadecartas.app.game.kemixDeck
import kotlin.random.Random

enum class KemixPhase { PLAYING, OVER }

class KemixGame(
    val names: List<String>,
    val botSeats: Set<Int>,
    seed: Long = System.currentTimeMillis()
) {
    private val rng = Random(seed)
    val n = names.size
    val cards = kemixDeck().shuffled(rng)
    val revealed = mutableSetOf<Int>()
    val matchedBy = IntArray(cards.size) { -1 }
    var pairs = IntArray(n)
        private set
    var current = 0
        private set
    var phase = KemixPhase.PLAYING
        private set
    var message = "Encuentra las parejas"
        private set
    var firstFlip = -1
        private set
    var secondFlip = -1
        private set
    var pendingHide = false
        private set
    var winner = -1
        private set
    private val seen = mutableMapOf<Int, Int>()

    fun flip(index: Int): Boolean {
        if (phase != KemixPhase.PLAYING || pendingHide) return false
        if (index !in cards.indices || index in revealed || matchedBy[index] >= 0) return false
        revealed += index
        seen[index] = cards[index].family
        if (firstFlip < 0) {
            firstFlip = index
            message = "${names[current]} volteó una carta"
            return true
        }
        if (index == firstFlip) return false
        secondFlip = index
        if (cards[firstFlip].family == cards[index].family) {
            matchedBy[firstFlip] = current
            matchedBy[index] = current
            pairs[current] += 1
            message = "¡Pareja para ${names[current]}!"
            firstFlip = -1
            secondFlip = -1
            checkEnd()
        } else {
            pendingHide = true
            message = "No coinciden…"
        }
        return true
    }

    fun resolveMismatch() {
        if (!pendingHide) return
        revealed.remove(firstFlip)
        revealed.remove(secondFlip)
        firstFlip = -1
        secondFlip = -1
        pendingHide = false
        current = (current + 1) % n
        message = "Turno de ${names[current]}"
    }

    private fun checkEnd() {
        if (matchedBy.all { it >= 0 }) {
            phase = KemixPhase.OVER
            winner = pairs.indices.maxByOrNull { pairs[it] } ?: 0
            message = "¡${names[winner]} gana Kemix con ${pairs[winner]} parejas!"
        }
    }

    fun botFirstPick(): Int {
        val free = cards.indices.filter { it !in revealed && matchedBy[it] < 0 }
        if (free.isEmpty()) return -1
        val byFamily = seen.filterKeys { it in free }.entries.groupBy { it.value }
        val pair = byFamily.values.firstOrNull { it.size >= 2 }
        if (pair != null) return pair.first().key
        return free.random(rng)
    }

    fun botSecondPick(first: Int): Int {
        val free = cards.indices.filter { it !in revealed && matchedBy[it] < 0 && it != first }
        if (free.isEmpty()) return -1
        val match = free.firstOrNull { cards[it].family == cards[first].family }
        if (match != null) return match
        val known = free.firstOrNull { seen[it] == cards[first].family }
        return known ?: free.random(rng)
    }

    fun isBotTurn(): Boolean = current in botSeats && phase == KemixPhase.PLAYING
}
