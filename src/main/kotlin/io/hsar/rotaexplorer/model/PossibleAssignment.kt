package io.hsar.rotaexplorer.model

class PossibleAssignment(val person: Person, val possibilityWeight: Double) : Comparable<PossibleAssignment> {
    override fun compareTo(other: PossibleAssignment): Int {
        return possibilityWeight.compareTo(other = other.possibilityWeight)
    }
}