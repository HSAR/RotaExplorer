package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * When possible, we should assign people to slots that are close to each other
 */
class ShouldAvoidSplitSlots : Rule() {

    companion object {
        private const val PENALTY_PER_NON_DOUBLED_SLOT = 0.2
        private const val HOURS_BETWEEN_CLOSE_TIME_SLOTS = 2.0
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        return assignments.filterByCommitted()
                .toList()
                .groupBy { (key, _) ->
                    key.startTime.truncatedTo(ChronoUnit.DAYS)
                }
                .map { (_, rotaSlotsOnOneDay) ->
                    rotaSlotsOnOneDay.map { (rotaSlotOnOneDay, personAssignedToOneDay) ->
                        rotaSlotsOnOneDay.mapNotNull { (rotaSlotToCheckAgainst, personToCheckAgainst) ->
                            // Check whether slots that are close to each other are assigned to different people
                            if (rotaSlotOnOneDay.startTime != rotaSlotToCheckAgainst.startTime &&
                                    ChronoUnit.HOURS.between(rotaSlotOnOneDay.startTime, rotaSlotToCheckAgainst.startTime) < HOURS_BETWEEN_CLOSE_TIME_SLOTS &&
                                    personAssignedToOneDay != personToCheckAgainst) {
                                orderedPair(rotaSlotOnOneDay.startTime.toEpochSecond(), rotaSlotToCheckAgainst.startTime.toEpochSecond())
                            } else {
                                null
                            }
                        }
                    }
                }
                .count()
                .let { numberOfNonDoubledSlots ->
                    numberOfNonDoubledSlots * PENALTY_PER_NON_DOUBLED_SLOT
                }
                .toDouble()
    }

    private fun orderedPair(elem1: Long, elem2: Long): Pair<Long, Long> {
        return min(elem1, elem2) to max(elem1, elem2)
    }
}