package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Assignment
import io.hsar.rotaexplorer.model.RotaSlot

abstract class Rule {

    /**
     * Given a rota with committed assignments, calculate a modifier to the weight according to the logic of this rule.
     * Returned result should always be positive.
     */
    abstract fun applyWeight(assignments: Map<RotaSlot, Assignment>): Double
}