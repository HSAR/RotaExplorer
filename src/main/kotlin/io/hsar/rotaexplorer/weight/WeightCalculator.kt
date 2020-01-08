package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Rota
import io.hsar.rotaexplorer.model.RotaSlot

object WeightCalculator {

    val rules = listOf(
            ShouldAvoidAvailableIfNeeded(),
            ShouldAvoidSplitSlots(),
            ShouldLimitTimesPersonServesPerDay(),
            ShouldNotAllocateMoreThanAverage()
    )

    /**
     * Given a rota, calculates the current cost (representing how suboptimal it is)
     */
    fun calculate(rota: Rota): Double {
        return rules
                .map { rule ->
                    rule.applyWeight(assignments = rota.assignments)
                }
                .sum()
    }
}

/**
 * Given a map of assignments, filters to only those which are Committed and casts it as such.
 */
fun Map<RotaSlot, Assignment>.filterByCommitted(): Map<RotaSlot, Assignment.Committed> {
    return this
            .filterValues { assignment ->
                (assignment is Assignment.Committed)
            }
            .mapValues { (_, assignment) ->
                assignment as Assignment.Committed
            }
}