package io.hsar.rotaexplorer

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

class RotaExplorer(val rotaSlotsToFill: List<RotaSlot>, private val responses: List<Response>) { // TODO: Unpick the assumption that responses has at least one entry for each rota slot to fill

    companion object {
        val logger = LoggerFactory.getLogger(RotaExplorer::class.java)
    }

    init {
        logger.info("Found ${rotaSlotsToFill.size} slots with ${responses.size} respondees to fill them with.")
    }

    fun execute(): PriorityQueue<Rota> {

        return PriorityQueue<Rota>()
                .also { emptyQueue ->
                    responses
                            .flatMap { response ->
                                response.rotaSlotsToAvailability
                                        .map { (rotaSlot, availability) ->
                                            // Available if needed is weighted for half, this produces desired weighting
                                            rotaSlot to when (availability) {
                                                AVAILABLE -> 1.0
                                                AVAILABLE_IF_NEEDED -> 0.5
                                                NOT_AVAILABLE -> 0.0
                                            }
                                        }
                                        .let { rotaSlotsToAvailabilityWeights ->
                                            // Individual votes are divided by the total vote weight, this makes people who voted for fewer days more likely to serve on those days
                                            rotaSlotsToAvailabilityWeights
                                                    .sumByDouble { it.second }
                                                    .let { totalAvailabilityWeight ->
                                                        rotaSlotsToAvailabilityWeights.map { (rotaSlot, availabilityWeight) ->
                                                            rotaSlot to PossibleAssignment(response.person, availabilityWeight / totalAvailabilityWeight)
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
                                        // Construct object for Rota construction
                                        .let { possibleAssignments ->
                                            Possibilities(PriorityQueue(possibleAssignments))
                                        }
                            }
                            .toMap()
                            .let { rotaSlotsToPossibilities ->
                                emptyQueue.add(Rota(rotaSlotsToPossibilities))
                            }
                }
    }


}