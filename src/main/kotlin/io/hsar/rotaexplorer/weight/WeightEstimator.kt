package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Rota
import io.hsar.rotaexplorer.model.RotaSlot

object WeightEstimator {

    /**
     * Given a rota, estimates its likely future cost heuristically
     */
    fun calculate(rota: Rota): Double {
        return rota.assignments.filterByPossibilities()
                .map { (_, possibilities) ->
                    (1.0 / possibilities.possiblePeople.count()) * 25 // Very crude, fewer possibilities is more expensive
                }
                .sum()
    }
}

/**
 * Given a map of assignments, filters to only those which are Possibilities and casts it as such.
 */
fun Map<RotaSlot, Assignment>.filterByPossibilities(): Map<RotaSlot, Assignment.Possibilities> {
    return this
            .filterValues { assignment ->
                (assignment is Assignment.Possibilities)
            }
            .mapValues { (_, assignment) ->
                assignment as Assignment.Possibilities
            }
}