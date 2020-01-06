package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot
import java.time.temporal.ChronoUnit

/**
 * When possible, we should assign people to slots that are close to each other
 */
class ShouldAvoidSplitSlots : Rule() {

    companion object {
        private const val PENALTY_PER_NON_DOUBLED_SLOT = 0.2
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments.filterByCommitted()
                .keys
                .groupBy { test ->
                    test.startTime.truncatedTo(ChronoUnit.DAYS)
                }
                .mapValues { (_, rotaSlotsOnOneDay) ->
                    rotaSlotsOnOneDay.map { rotaSlotOnOneDay ->
                        rotaSlotsOnOneDay.mapNotNull { rotaSlotToCheckAgainst ->
                            // #TODO: Check slots that are close to each other, and if different people are doing it then add them
                            null
                        }
                    }
                }
                .count()
                .let { numberOfAvailableIfNeededUsed ->
                    numberOfAvailableIfNeededUsed * PENALTY_PER_NON_DOUBLED_SLOT
                }
                .toDouble()
    }
}