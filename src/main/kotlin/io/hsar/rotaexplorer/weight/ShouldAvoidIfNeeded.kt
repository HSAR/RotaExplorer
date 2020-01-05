package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Availability.AVAILABLE_IF_NEEDED
import io.hsar.rotaexplorer.model.RotaSlot

/**
 * We should always prefer AVAILABLE to AVAILABLE_IF_NEEDED
 */
class ShouldAvoidIfNeeded : Rule {

    companion object {
        const val PENALTY_PER_IF_NEEDED = 1
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments
                .values
                .filter { assignment ->
                    (assignment is Assignment.Committed) && assignment.commitment.personAvailability == AVAILABLE_IF_NEEDED
                }
                .count()
                .let { numberOfIfNeededs ->
                    numberOfIfNeededs * PENALTY_PER_IF_NEEDED
                }
                .toDouble()
    }
}