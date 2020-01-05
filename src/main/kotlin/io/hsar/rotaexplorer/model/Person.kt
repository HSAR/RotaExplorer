package io.hsar.rotaexplorer.model

class Person(val name: String, needsSupervision: Supervision)

enum class Supervision { SUPERVISION_REQUIRED, SUPERVISION_NOT_REQUIRED, SUPERVISION_FIRST_TIME }