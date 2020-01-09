package io.hsar.rotaexplorer.model

import java.util.PriorityQueue

class PossibleRotaNavigation(initialRota: Rota) {

    private val rotaPriorityQueue = PriorityQueue(listOf(initialRota))
    val size: Int
        get() = rotaPriorityQueue.size

    private val rotaSet = mutableSetOf(initialRota) // #TODO: Use BloomFilter

    fun add(rota: Rota): PossibleRotaNavigation {
        return this.also {
            // Only add the new rota if it does not already exist in this object
            if (!rotaSet.contains(rota)) {
                rotaSet.add(rota)
                rotaPriorityQueue.add(rota)
            }
        }
    }

    fun peek(): Rota = rotaPriorityQueue.peek()

    // NB: We do not remove from the Set, it always keeps track of everything we have explored
    fun poll(): Rota = rotaPriorityQueue.poll()
}