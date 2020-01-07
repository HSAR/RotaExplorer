package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Availability
import io.hsar.rotaexplorer.model.RotaSlot

/**
 * We should always prefer AVAILABLE to AVAILABLE_IF_NEEDED
 */
class ShouldAvoidAvailableIfNeeded : Rule() {

    companion object {
        private const val PENALTY_PER_AVAILABLE_IF_NEEDED = 1.0
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments.filterByCommitted()
                .values
                .filter { assignment ->
                    assignment.commitment.personAvailability == Availability.AVAILABLE_IF_NEEDED
                }
                .count()
                .let { numberOfAvailableIfNeededUsed ->
                    numberOfAvailableIfNeededUsed * PENALTY_PER_AVAILABLE_IF_NEEDED
                }
                .toDouble()
    }
}