package io.hsar.rotaexplorer.weight

import io.hsar.rotaexplorer.model.Rota

object WeightCalculator {

    val rules = listOf(
            ShouldAvoidAvailableIfNeeded(),
            ShouldAvoidSplitSlots(),
            ShouldLimitTimesPersonServesPerDay()
    )

    fun calculate(rota: Rota): Double {
        return rules
                .map { rule ->
                    rule.applyWeight(assignments = rota.assignments)
                }
                .sum()
    }
}