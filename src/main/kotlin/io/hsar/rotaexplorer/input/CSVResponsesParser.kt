package io.hsar.rotaexplorer.input

import io.hsar.rotaexplorer.model.Availability.AVAILABLE
import io.hsar.rotaexplorer.model.Availability.AVAILABLE_IF_NEEDED
import io.hsar.rotaexplorer.model.Availability.NOT_AVAILABLE
import io.hsar.rotaexplorer.model.Person
import io.hsar.rotaexplorer.model.Response
import io.hsar.rotaexplorer.model.RotaSlot
import io.hsar.rotaexplorer.model.Supervision.SUPERVISION_NOT_REQUIRED
import io.hsar.rotaexplorer.util.zip
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVParser
import java.io.File
import java.nio.charset.Charset
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * Given a CSV file, parses the first three rows for date and time information, then the others as Response objects according to their columns.
 * (If this sounds strange and arbitrary, this is because Doodle polls converted to CSV look like this)
 */
class CSVResponsesParser(private val separatorChar: Char = ',') {

    private val format = CSVFormat.newFormat(separatorChar)

    fun parseFile(file: File): List<Response> {
        return CSVParser.parse(file, Charset.defaultCharset(), format)
                .records
                .map { parsedLine ->
                    parsedLine.map { field -> field.toString() }
                }
                .let { parsedLines ->
                    val rotaSlotTimes = parsedLines.take(3) // First three lines should contain our time information
                            .let { timeInformationRows ->
                                // Extract raw strings, discarding the first field of each line (they are expected to be empty)
                                val monthAndYearString = timeInformationRows[0].drop(1)
                                val dayAndDate = timeInformationRows[1].drop(1)
                                val startTimeAndEndTime = timeInformationRows[2].drop(1)
                                        .map { startTimeAndEndTimeString ->
                                            // Fields look like: "9:30 - 10:30"
                                            startTimeAndEndTimeString.split(" - ")
                                                    .first() // But we only care about "9:30"
                                        }


                                // Combine the datestamps, which are spread across three rows, into one string
                                val joinedRotaTimeStrings = zip(monthAndYearString, dayAndDate, startTimeAndEndTime)
                                        .map { datestampFields ->
                                            datestampFields.joinToString(" ")
                                        }

                                // Parse fields
                                val parsedRotaTimes = DoodleDateStampParser.parse(joinedRotaTimeStrings)

                                // Merge fields to ZonedDateTime objects
                                parsedRotaTimes
                                        // Construct RotaSlot objects
                                        .map { zonedDateTime ->
                                            RotaSlot(zonedDateTime)
                                        }
                            }

                    parsedLines.drop(3) // Everything but the first three lines are responses
                            .map { fieldsInLine ->
                                val person = Person(
                                        name = fieldsInLine.first(),
                                        needsSupervision = SUPERVISION_NOT_REQUIRED
                                )

                                val availabilities = fieldsInLine.drop(1) // Already used the first field
                                        .map { responseString ->
                                            when (responseString) {
                                                "OK" -> AVAILABLE
                                                "(OK)" -> AVAILABLE_IF_NEEDED
                                                "" -> NOT_AVAILABLE
                                                else -> throw IllegalArgumentException("Unrecognised availability string: '$responseString'.")
                                            }
                                        }

                                Response(person, rotaSlotTimes.zip(availabilities).toMap())
                            }
                }

    }
}

object DoodleDateStampParser {
    private const val PATTERN = "MMMM yyyy EEE d HH:mm"
    val parser = DateTimeFormatter
            .ofPattern(PATTERN)
            .withZone(ZoneId.systemDefault())

    fun parse(fields: List<String>): List<ZonedDateTime> {
        return fields.map { field ->
            ZonedDateTime.from(parser.parse(field))
        }
    }
}