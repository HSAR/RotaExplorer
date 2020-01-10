package io.hsar.rotaexplorer

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.model.Assignment.Possibilities
import io.hsar.rotaexplorer.model.Availability.AVAILABLE
import io.hsar.rotaexplorer.model.Availability.AVAILABLE_IF_NEEDED
import io.hsar.rotaexplorer.model.Availability.NOT_AVAILABLE
import io.hsar.rotaexplorer.model.PossibleAssignment
import io.hsar.rotaexplorer.model.PossibleRotaNavigation
import io.hsar.rotaexplorer.model.Response
import io.hsar.rotaexplorer.model.Rota
import io.hsar.rotaexplorer.model.RotaSlot
import org.slf4j.LoggerFactory

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
        PossibleRotaNavigation(generateInitialRota())
                .let { possibleRotaNavigation ->
                    while (!possibleRotaNavigation.peek().isComplete) {
                        possibleRotaNavigation
                                .poll() // After we have dealt with a possible rota, we will queue all the resulting options
                                .let { possibleRota ->
                                    if (System.currentTimeMillis() % 1000 == 0L) {
                                        logger.debug("Trying new possibility: weight ${possibleRota.weight} and ${possibleRota.assignmentsMade} / ${possibleRota.assignments.size} assignments made.")
                                    }

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
                                                        .filterNot { possibleAssignment -> possibleAssignment.possibilityWeight == 0.0 }
                                                        .sortedBy { possibleAssignment -> possibleAssignment.possibilityWeight }
                                                        .take(3) // Explore top three possibilities
                                                        .map { possibleAssignment ->
                                                            // For each possible person, make a new theoretical assignment and create a Rota
                                                            Rota((possibleRota.assignments + mapOf(rotaSlot to Committed(possibleAssignment))))
                                                        }
                                            }
                                }
                                .map { newPossibleRota ->
                                    possibleRotaNavigation.add(newPossibleRota)
                                }
                    }

                    return possibleRotaNavigation.peek().also { finalRota ->
                        logger.info("Selected rota with final weight of ${finalRota.weight}.")
                    }
                }
    }

    private fun generateInitialRota(): Rota {
        return responses
                .flatMap { response ->
                    response.rotaSlotsToAvailability
                            .mapNotNull { (rotaSlot, availability) ->
                                // Available if needed is weighted for half, this produces desired weighting
                                val availabilityWeight = when (availability) {
                                    AVAILABLE -> 1.0
                                    AVAILABLE_IF_NEEDED -> 0.5
                                    NOT_AVAILABLE -> null
                                }

                                // #TODO: There must be a better way to do this
                                if (availabilityWeight == null) {
                                    null
                                } else {
                                    Triple(
                                            first = rotaSlot,
                                            second = availabilityWeight,
                                            third = availability
                                    )
                                }
                            }
                            .let { rotaSlotsToAvailabilityWeights ->
                                // Individual votes are divided by the total vote weight, this makes people who voted for fewer days more likely to serve on those days
                                rotaSlotsToAvailabilityWeights
                                        .sumByDouble { (_, weight, _) ->
                                            weight
                                        }
                                        .let { totalAvailabilityWeight ->
                                            rotaSlotsToAvailabilityWeights.map { (rotaSlot, availabilityWeight, availability) ->
                                                // Selection is by lowest first, so people who have voted the least will be assigned early
                                                rotaSlot to PossibleAssignment(
                                                        person = response.person,
                                                        possibilityWeight = totalAvailabilityWeight,
                                                        personAvailability = availability
                                                )
                                            }
                                        }
                            }
                }
                .groupBy { (rotaSlot, _) -> rotaSlot }
                .map { (rotaSlot, rotaSlotToPossibleAssignments) ->
                    rotaSlot to rotaSlotToPossibleAssignments
                            .map { (_, possibleAssignment) ->
                                possibleAssignment
                            }
                            .toSet()
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
                        Rota(allRotaSlots)
                    }
                }
    }
}