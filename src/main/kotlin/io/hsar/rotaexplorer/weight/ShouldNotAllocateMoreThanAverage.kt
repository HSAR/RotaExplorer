package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.model.Assignment.Possibilities
import io.hsar.rotaexplorer.model.RotaSlot
import kotlin.math.pow

/**
 * People should not serve more than average.
 */
class ShouldNotAllocateMoreThanAverage : Rule() {

    companion object {
        private const val PENALTY_MULTIPLIER_PER_ABOVE_AVERAGE = 1.0
    }

    override fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double {
        val numberOfPeople = assignments.values
                .map { assignment ->
                    when (assignment) {
                        is Committed -> listOf(assignment.commitment)
                        is Possibilities -> assignment.possiblePeople
                    }.map { possibleAssignment ->
                        possibleAssignment.person
                    }
                }
                .reduce { setA, setB -> setA + setB }
                .distinct() // Count unique people
                .count()
                .toDouble()

        val numberOfRotaSlots = assignments.size
                .toDouble()

        val expectedAverage = numberOfRotaSlots / numberOfPeople

        return assignments.filterByCommitted()
                .values
                .groupBy { commitment ->
                    commitment.commitment.person
                }
                .map { (_, commitmentsPerPerson) ->
                    commitmentsPerPerson.count()
                }
                .map { numberOfCommitmentsPerPerson ->
                    // Apply principle of least squares - penalise someone that serves twice above average four times more than once above average
                    (expectedAverage - numberOfCommitmentsPerPerson).pow(2) * PENALTY_MULTIPLIER_PER_ABOVE_AVERAGE
                }
                .sum()
    }
}