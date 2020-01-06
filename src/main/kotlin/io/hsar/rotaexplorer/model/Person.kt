package io.hsar.rotaexplorer.model

data class Person(val name: String, val needsSupervision: Supervision)

enum class Supervision {
    SUPERVISION_FIRST_TIME,
    SUPERVISION_REQUIRED,
    SUPERVISION_NOT_REQUIRED,
    SUPERVISOR // TODO: To be used for https://github.com/HSAR/RotaExplorer/issues/1
}