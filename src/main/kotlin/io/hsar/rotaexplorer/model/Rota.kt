package io.hsar.rotaexplorer.model

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.weight.WeightCalculator
import io.hsar.rotaexplorer.weight.WeightEstimator

data class Rota(val assignments: Map<RotaSlot, Assignment>) : Comparable<Rota> {

    init {
        assignments
                .let { newAssignments ->
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

    val weight: Double = WeightCalculator.calculate(rota = this) + WeightEstimator.calculate(rota = this)

    val isComplete: Boolean
        get() = assignments.values.all { assignment -> assignment is Committed }

    val assignmentsMade: Int
        get() = assignments.values.count { assignment -> assignment is Committed }

    override fun compareTo(other: Rota): Int {
        return this.weight.compareTo(other.weight)
    }
}