package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlin.math.min

/**
 * One person should not serve more than once a day.
 */
class ShouldLimitTimesPersonServesPerDay : Rule() {

    companion object {
        private const val PENALTY_FOR_EXTENSIVE_SERVING = 12.1
        private const val HOURS = 4.0
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
                            if (rotaSlotOnOneDay.startTime != rotaSlotToCheckAgainst.startTime &&
                                    personAssignedToOneDay == personToCheckAgainst &&
                                    ChronoUnit.HOURS.between(rotaSlotOnOneDay.startTime, rotaSlotToCheckAgainst.startTime) > HOURS) {
                                orderedPair(rotaSlotOnOneDay.startTime.toEpochSecond(), rotaSlotToCheckAgainst.startTime.toEpochSecond())
                            } else {
                                null
                            }
                        }
                    }
                }
                .count()
                .let { numberOfPeopleWhoServeMultipleTimesADay ->
                    numberOfPeopleWhoServeMultipleTimesADay * ShouldLimitTimesPersonServesPerDay.PENALTY_FOR_EXTENSIVE_SERVING
                }
                .toDouble()
    }

    private fun orderedPair(elem1: Long, elem2: Long): Pair<Long, Long> {
        return min(elem1, elem2) to max(elem1, elem2)
    }
}