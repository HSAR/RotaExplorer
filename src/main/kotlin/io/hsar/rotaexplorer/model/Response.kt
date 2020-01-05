package io.hsar.rotaexplorer.model

data class Response(val person: Person, val rotaSlotsToAvailability: Map<RotaSlot, Availability>)