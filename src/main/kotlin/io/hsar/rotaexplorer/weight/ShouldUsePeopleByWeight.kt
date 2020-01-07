package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot

class ShouldUsePeopleByWeight : Rule() {
    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments.filterByCommitted()
                .values
                .sumByDouble { commitedAssignment ->
                    commitedAssignment.commitment.possibilityWeight
                }
    }
}