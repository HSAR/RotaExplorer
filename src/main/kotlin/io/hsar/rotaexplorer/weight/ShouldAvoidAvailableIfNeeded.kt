package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot

/**
 * We should always prefer AVAILABLE to AVAILABLE_IF_NEEDED
 */
class ShouldAvoidAvailableIfNeeded : Rule() {

    companion object {
        private const val PENALTY_PER_AVAILABLE_IF_NEEDED = 1
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments.filterByCommitted()
                .values
                .count()
                .let { numberOfAvailableIfNeededUsed ->
                    numberOfAvailableIfNeededUsed * PENALTY_PER_AVAILABLE_IF_NEEDED
                }
                .toDouble()
    }
}