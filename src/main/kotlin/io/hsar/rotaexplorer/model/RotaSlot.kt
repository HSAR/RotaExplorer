package io.hsar.rotaexplorer.model

import java.time.ZonedDateTime

data class RotaSlot(val startTime: ZonedDateTime) : Comparable<RotaSlot> {

    override fun compareTo(other: RotaSlot): Int {
        return startTime.compareTo(other.startTime)
    }

}
