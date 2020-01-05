package io.hsar.rotaexplorer.model

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.weight.WeightCalculator

class Rota : Comparable<Rota> {

    constructor(assignments: Map<RotaSlot, Assignment>) {
        this.assignments = assignments
                .also { newAssignments ->
                    newAssignments
                            .map { (_, assignment) ->
                                when (assignment) {
                                    is Committed -> Unit // Do nothing
                                    is Assignment.Possibilities -> if (assignment.possiblePeople.isEmpty()) {
                                        throw IllegalArgumentException("Should not have any rota slots with no possible people.")
                                    }
                                }
                            }
                }
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