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

/** HU-01: intervalo y días activos del recordatorio periódico. */
data class ReminderScheduleConfig(
    val intervalMin: Int = DEFAULT_INTERVAL,
    val activeDays: Set<Int> = DIAS_LABORALES,
) {
    fun isActiveToday(now: Long = System.currentTimeMillis()): Boolean {
        val day = java.util.Calendar.getInstance().apply { timeInMillis = now }.get(java.util.Calendar.DAY_OF_WEEK)
        return day in activeDays
    }

    companion object {
        const val MIN_INTERVAL = 15
        const val DEFAULT_INTERVAL = 60
        val INTERVALOS_PREDEFINIDOS = listOf(30, 45, 60, 90)
        val DIAS_LABORALES = setOf(
            java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY, java.util.Calendar.WEDNESDAY,
            java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY,
        )

        fun nombreDia(dia: Int): String = when (dia) {
            java.util.Calendar.MONDAY -> "Lunes"
            java.util.Calendar.TUESDAY -> "Martes"
            java.util.Calendar.WEDNESDAY -> "Miércoles"
            java.util.Calendar.THURSDAY -> "Jueves"
            java.util.Calendar.FRIDAY -> "Viernes"
            java.util.Calendar.SATURDAY -> "Sábado"
            java.util.Calendar.SUNDAY -> "Domingo"
            else -> "Día"
        }

        fun ordenSemana(): List<Int> = listOf(
            java.util.Calendar.MONDAY, java.util.Calendar.TUESDAY, java.util.Calendar.WEDNESDAY,
            java.util.Calendar.THURSDAY, java.util.Calendar.FRIDAY, java.util.Calendar.SATURDAY,
            java.util.Calendar.SUNDAY,
        )

        fun daysToMask(days: Set<Int>): Int = days.fold(0) { acc, d -> acc or (1 shl (d - 1)) }
        fun maskToDays(mask: Int): Set<Int> = (1..7).filter { mask and (1 shl (it - 1)) != 0 }.toSet()
        fun defaultDaysMask(): Int = daysToMask(DIAS_LABORALES)
    }
}

/** HU-15: dos perfiles de horario (solo uno activo). */
data class ScheduleProfile(
    val nombre: String,
    val intervalMin: Int = ReminderScheduleConfig.DEFAULT_INTERVAL,
    val activeDays: Set<Int> = ReminderScheduleConfig.DIAS_LABORALES,
) {
    fun toSchedule() = ReminderScheduleConfig(intervalMin, activeDays)
}

data class ScheduleProfilesState(
    val perfil1: ScheduleProfile = ScheduleProfile("Perfil 1"),
    val perfil2: ScheduleProfile = ScheduleProfile("Perfil 2"),
    val indiceActivo: Int = 0,
) {
    val activo: ScheduleProfile get() = if (indiceActivo == 1) perfil2 else perfil1
}

enum class TemaApp { CLARO, OSCURO, SISTEMA }

enum class EscalaTexto(val factor: Float) {
    NORMAL(1f), GRANDE(1.2f), EXTRA_GRANDE(1.4f),
}

data class AparienciaApp(val tema: TemaApp = TemaApp.SISTEMA, val escalaTexto: EscalaTexto = EscalaTexto.NORMAL)

/** HU-14: recordatorios de hidratación independientes. */
data class ConfigHidratacion(val activo: Boolean = false, val intervaloMin: Int = 60) {
    companion object { val INTERVALOS = listOf(60, 120) }
}
