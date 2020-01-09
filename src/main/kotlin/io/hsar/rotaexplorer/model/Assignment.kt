package io.hsar.rotaexplorer.model

sealed class Assignment {
    data class Committed(val commitment: PossibleAssignment) : Assignment()
    data class Possibilities(val possiblePeople: Set<PossibleAssignment> = emptySet()) : Assignment()
}