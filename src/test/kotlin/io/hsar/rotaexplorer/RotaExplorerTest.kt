package io.hsar.rotaexplorer

import io.hsar.rotaexplorer.model.Assignment.Committed
import io.hsar.rotaexplorer.model.Availability.AVAILABLE
import io.hsar.rotaexplorer.model.Availability.AVAILABLE_IF_NEEDED
import io.hsar.rotaexplorer.model.Availability.NOT_AVAILABLE
import io.hsar.rotaexplorer.model.Person
import io.hsar.rotaexplorer.model.Response
import io.hsar.rotaexplorer.model.RotaSlot
import io.hsar.rotaexplorer.model.Supervision.SUPERVISION_FIRST_TIME
import io.hsar.rotaexplorer.model.Supervision.SUPERVISION_NOT_REQUIRED
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Test
import java.time.ZoneId.systemDefault
import java.time.ZonedDateTime

internal class RotaExplorerTest {

    @Test
    fun `should correctly allocate rota slots in a simple case`() {
        // Arrange -----
        val rotaSlot1 = RotaSlot(ZonedDateTime.of(2020, 1, 12, 9, 30, 0, 0, systemDefault()))
        val rotaSlot2 = RotaSlot(ZonedDateTime.of(2020, 1, 12, 11, 15, 0, 0, systemDefault()))

        val testPersonA = Person("Alice", SUPERVISION_FIRST_TIME)
        val testPersonB = Person("Bob", SUPERVISION_NOT_REQUIRED)
        val testPersonC = Person("Charlie", SUPERVISION_NOT_REQUIRED)
        val testPersonD = Person("David", SUPERVISION_NOT_REQUIRED)
        val testResponses = listOf(
                Response(testPersonA, mapOf(rotaSlot1 to AVAILABLE, rotaSlot2 to AVAILABLE)),
                Response(testPersonB, mapOf(rotaSlot1 to AVAILABLE, rotaSlot2 to AVAILABLE_IF_NEEDED)),
                Response(testPersonC, mapOf(rotaSlot1 to AVAILABLE, rotaSlot2 to NOT_AVAILABLE)),
                Response(testPersonD, mapOf(rotaSlot1 to AVAILABLE_IF_NEEDED, rotaSlot2 to NOT_AVAILABLE))
        )

        val objectUnderTest = RotaExplorer(rotaSlotsToFill = listOf(rotaSlot1, rotaSlot2), responses = testResponses)

        // Act -----
        val actualRota = objectUnderTest.execute()

        // Assert -----
        val expectedAssignments = mapOf(
                rotaSlot1 to testPersonC, // Charlie responded YES only to slot 1
                rotaSlot2 to testPersonA // Alice responded YES to slot 1 and slot 2
        )

        assertThat(actualRota.isComplete, equalTo(true))
        expectedAssignments.forEach { (rotaSlot, expectedPerson) ->
            val actualPerson = (actualRota.assignments[rotaSlot]!! as Committed).commitment.person
            assertThat("Expected $expectedPerson to have been assigned to $rotaSlot, but was $actualPerson", actualPerson, equalTo(expectedPerson))
        }
    }
}