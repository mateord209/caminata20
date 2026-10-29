package com.example.caminata20.domain.model

/** Usuario autenticado (Firebase Auth). */
data class AuthUser(val uid: String, val name: String?, val email: String?)

/** Perfil + meta diaria (HU-12). */
data class Profile(val name: String = "", val email: String = "", val dailyGoal: Int = DEFAULT_GOAL) {
    companion object {
        val GOAL_RANGE = 2..15
        const val DEFAULT_GOAL = 8
    }
}

/** Preferencias de pausa (HU-08). */
data class PauseSettings(val type: PauseType = PauseType.ESTIRAMIENTO, val durationMin: Int = 2)

/** Hora del recordatorio diario (pantalla Recordatorio). */
data class ReminderTime(val hour: Int, val minute: Int) {
    val label: String get() = "%02d:%02d".format(hour, minute)
}

enum class PauseStatus { COMPLETADA, POSPUESTA }

/** Registro de pausa (historial mínimo HU-03; historial completo TODO HU-04). */
data class PauseRecord(
    val timestamp: Long,
    val type: PauseType,
    val status: PauseStatus,
    val exerciseName: String = "",
    val id: String = "",
)

/** Caminata registrada por WalkTrackingService. */
data class WalkSession(val timestamp: Long, val steps: Int, val km: Double, val id: String = "")

/** Totales calculados a partir de las caminatas. */
data class WalkStats(val totalSteps: Int = 0, val totalKm: Double = 0.0, val weekKm: Double = 0.0)

/** Ejercicio generado por IA (Groq). */
data class AiExercise(val name: String, val category: String, val sets: Int, val reps: Int, val instructions: String = "")

/** Rutina de IA completada. */
data class AiRoutine(val timestamp: Long, val exercises: List<AiExercise>, val id: String = "")

/** Resultado de operaciones que pueden fallar con un mensaje para el usuario. */
sealed interface Outcome<out T> {
    data class Success<T>(val value: T) : Outcome<T>
    data class Failure(val message: String) : Outcome<Nothing>
}
