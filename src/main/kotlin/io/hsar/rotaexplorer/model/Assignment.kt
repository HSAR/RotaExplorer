package io.hsar.rotaexplorer.model

sealed class Assignment {
    class Committed(val commitment: PossibleAssignment) : Assignment()
    class Possibilities(val possiblePeople: List<PossibleAssignment> = emptyList()) : Assignment()
}