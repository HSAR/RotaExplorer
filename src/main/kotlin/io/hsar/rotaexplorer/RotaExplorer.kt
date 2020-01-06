package io.hsar.rotaexplorer

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.model.Assignment.Possibilities
import io.hsar.rotaexplorer.model.Availability.AVAILABLE
import io.hsar.rotaexplorer.model.Availability.AVAILABLE_IF_NEEDED
import io.hsar.rotaexplorer.model.Availability.NOT_AVAILABLE
import io.hsar.rotaexplorer.model.PossibleAssignment
import io.hsar.rotaexplorer.model.Response
import io.hsar.rotaexplorer.model.Rota
import io.hsar.rotaexplorer.model.RotaSlot
import org.slf4j.LoggerFactory
import java.util.PriorityQueue

class RotaExplorer(private val rotaSlotsToFill: List<RotaSlot> = emptyList(), private val responses: List<Response>) {

    companion object {
        val logger = LoggerFactory.getLogger(RotaExplorer::class.java)
    }

    init {
        logger.info("Found ${rotaSlotsToFill.size} slots with ${responses.size} respondees to fill them with.")
    }

    /**
     * Given a set of rota slots to fill and a list of responses, returns the rota with the fewest undesirable choices
     */
    fun execute(): Rota {
        preparePriorityQueue()
                .let { priorityQueue ->
                    while (!priorityQueue.peek().isComplete) {
                        priorityQueue
                                .poll() // After we have dealt with a possible rota, we will queue all the resulting options
                                .let { possibleRota ->
                                    possibleRota
                                            .assignments
                                            .filter { (_, assignment) ->
                                                assignment is Possibilities
                                            }
                                            .mapValues { (_, assignment) ->
                                                assignment as Possibilities // Kotlin should have smart cast but it hasn't
                                            }
                                            .minBy { (_, possibilities) ->
                                                possibilities.possiblePeople.size  // We want to select the slot with the fewest possibilities to start with
                                            }!!
                                            .let { (rotaSlot, possibilities) ->
                                                possibilities.possiblePeople
                                                        .groupBy { possibleAssignment ->
                                                            possibleAssignment.possibilityWeight
                                                        }
                                                        .filterNot { (weight, _) -> weight == 0.0 }
                                                        .maxBy { (weight, _) -> weight }!! // Heaviest possibilities (if tied) are all selected
                                                        .value // We don't care about the weight after selecting the heaviest
                                                        .map { possibleAssignment ->
                                                            // For each possible person at the heaviest weight, make a new theoretical assignment and create a Rota
                                                            (possibleRota.assignments + mapOf(rotaSlot to Committed(possibleAssignment)))
                                                                    .let { newAssignments ->
                                                                        Rota(newAssignments)
                                                                    }
                                                        }
                                            }
                                }
                                .map { newPossibleRota ->
                                    logger.debug("Trying new possibility with weight ${newPossibleRota.weight}")
                                    priorityQueue.add(newPossibleRota)
                                }
                    }

                    return priorityQueue.peek().also { finalRota ->
                        logger.info("Selected rota with final weight of ${finalRota.weight}")
                    }
                }
    }

    private fun preparePriorityQueue(): PriorityQueue<Rota> {
        return responses
                .flatMap { response ->
                    response.rotaSlotsToAvailability
                            .map { (rotaSlot, availability) ->
                                // Available if needed is weighted for half, this produces desired weighting
                                Triple(
                                        first = rotaSlot,
                                        second = when (availability) {
                                            AVAILABLE -> 1.0
                                            AVAILABLE_IF_NEEDED -> 0.5
                                            NOT_AVAILABLE -> 0.0
                                        },
                                        third = availability
                                )
                            }
                            .let { rotaSlotsToAvailabilityWeights ->
                                // Individual votes are divided by the total vote weight, this makes people who voted for fewer days more likely to serve on those days
                                rotaSlotsToAvailabilityWeights
                                        .sumByDouble { (_, weight, _) ->
                                            weight
                                        }
                                        .let { totalAvailabilityWeight ->
                                            rotaSlotsToAvailabilityWeights.map { (rotaSlot, availabilityWeight, availability) ->
                                                rotaSlot to PossibleAssignment(
                                                        person = response.person,
                                                        possibilityWeight = availabilityWeight / totalAvailabilityWeight,
                                                        personAvailability = availability
                                                )
                                            }
                                        }
                            }
                }
                .groupBy { (rotaSlot, _) -> rotaSlot }
                .map { (rotaSlot, rotaSlotToPossibleAssignments) ->
                    rotaSlot to rotaSlotToPossibleAssignments
                            .toMap()
                            .map { (_, possibleAssignment) ->
                                possibleAssignment
                            }
                            // Construct object for Rota initialisation
                            .let { possibleAssignments ->
                                Possibilities(possibleAssignments)
                            }
                }
                .toMap()
                .let { rotaSlotsWithPossibilities ->
                    // In case there are any rota slots to fill that are not covered by responses, merge them in now
                    rotaSlotsToFill.associateWith { rotaSlotToFill ->
                        Possibilities() // No possibilities
                    }.let { rotaSlotsWithoutPossibilities ->
                        // Order of adding ensures that slots with possibilities write over slots without
                        rotaSlotsWithoutPossibilities + rotaSlotsWithPossibilities
                    }.let { allRotaSlots ->
                        // Finish by adding the now-initialised Rota to the previously-created priority queue
                        PriorityQueue(listOf(Rota(allRotaSlots)))
                    }
                }
    }
}