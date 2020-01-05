package io.hsar.rotaexplorer.model

sealed class Assignment {
    class Committed(val person: Person) : Assignment()
    class Possibilities(val possiblePeople: List<PossibleAssignment> = emptyList()) : Assignment()
}