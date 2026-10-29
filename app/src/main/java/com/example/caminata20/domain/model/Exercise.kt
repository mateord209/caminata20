package com.example.caminata20.domain.model

/** HU-16: ejercicio del catálogo local. */
data class Exercise(
    val id: String,
    val type: PauseType,
    val name: String,
    val area: String,
    val durationSec: Int,
    val steps: List<String>,
    val level: String = "Nivel fácil",
) {
    /** Instrucciones resumidas para la notificación. */
    val instructions: String get() = steps.joinToString(" ")
    val durationLabel: String get() = if (durationSec < 60) "$durationSec s" else "${durationSec / 60} min"
}
