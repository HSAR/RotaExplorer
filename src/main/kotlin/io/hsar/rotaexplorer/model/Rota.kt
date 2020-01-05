package io.hsar.rotaexplorer.model

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.weight.WeightCalculator

class Rota : Comparable<Rota> {

    constructor(assignments: Map<RotaSlot, Assignment>) {
        this.assignments = assignments
    }

    constructor(rotaSlotsToFill: List<RotaSlot>) {
        this.assignments = rotaSlotsToFill
                .map { rotaSlot ->
                    rotaSlot to Assignment.Possibilities()
                }
                .toMap()
    }

    val assignments: Map<RotaSlot, Assignment>

    val weight: Double
        get() = WeightCalculator.calculate(rota = this)

    val isComplete: Boolean
        get() = assignments.values.all { assignment -> assignment is Committed }

    override fun compareTo(other: Rota): Int {
        return this.weight.compareTo(other.weight)
    }
}