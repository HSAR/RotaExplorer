package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.model.RotaSlot

abstract class Rule {

    /**
     * Given a rota with committed assignments, calculate a modifier to the weight according to the logic of this rule.
     * Returned result should always be positive.
     */
    abstract fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double

    protected fun Map<RotaSlot, Assignment>.filterByCommitted(): Map<RotaSlot, Committed> {
        return this
                .filterValues { assignment ->
                    (assignment is Committed)
                }
                .mapValues { (_, assignment) ->
                    assignment as Committed
                }
    }
}