package io.hsar.rotaexplorer.model

import java.util.PriorityQueue

sealed class Assignment {
    class Committed(val person: Person) : Assignment()
    class Possibilities(val possiblePeople: PriorityQueue<PossibleAssignment> = PriorityQueue()) : Assignment()
}