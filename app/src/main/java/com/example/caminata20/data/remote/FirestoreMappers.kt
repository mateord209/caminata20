package com.example.caminata20.data.remote

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseSettings
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.AparienciaApp
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.EscalaTexto
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.ReminderTime
import com.example.caminata20.domain.model.ScheduleProfile
import com.example.caminata20.domain.model.ScheduleProfilesState
import com.example.caminata20.domain.model.TemaApp
import com.example.caminata20.domain.model.WalkSession

/** Conversión documento Firestore (Map) <-> modelo de dominio. Funciones puras para poder testearlas. */
object FirestoreMappers {
    fun profile(data: Map<String, Any?>?, fallbackName: String?, email: String?): Profile = Profile(
        name = (data?.get("name") as? String)?.takeIf { it.isNotBlank() } ?: fallbackName.orEmpty(),
        email = email.orEmpty(),
        dailyGoal = ((data?.get("dailyGoal") as? Number)?.toInt() ?: Profile.DEFAULT_GOAL).coerceIn(Profile.GOAL_RANGE),
    )

    fun pauseSettings(data: Map<String, Any?>?): PauseSettings = PauseSettings(
        type = PauseType.fromName(data?.get("pauseType") as? String),
        durationMin = ((data?.get("pauseDurationMin") as? Number)?.toInt() ?: 2).coerceIn(1, 3),
    )

    fun reminder(data: Map<String, Any?>?): ReminderTime? {
        val h = (data?.get("reminderHour") as? Number)?.toInt() ?: return null
        if (h < 0) return null
        return ReminderTime(h, (data["reminderMinute"] as? Number)?.toInt() ?: 0)
    }

    fun reminderSchedule(data: Map<String, Any?>?): ReminderScheduleConfig {
        val interval = (data?.get("intervalMin") as? Number)?.toInt() ?: ReminderScheduleConfig.DEFAULT_INTERVAL
        val mask = (data?.get("activeDaysMask") as? Number)?.toInt() ?: ReminderScheduleConfig.defaultDaysMask()
        return ReminderScheduleConfig(
            interval.coerceIn(ReminderScheduleConfig.MIN_INTERVAL, 240),
            ReminderScheduleConfig.maskToDays(mask),
        )
    }

    fun reminderScheduleToMap(config: ReminderScheduleConfig): Map<String, Any> = mapOf(
        "intervalMin" to config.intervalMin,
        "activeDaysMask" to ReminderScheduleConfig.daysToMask(config.activeDays),
    )

    private fun scheduleProfile(data: Map<String, Any?>?, prefix: String, defaultName: String): ScheduleProfile {
        val name = (data?.get("${prefix}Name") as? String)?.takeIf { it.isNotBlank() } ?: defaultName
        val interval = (data?.get("${prefix}Interval") as? Number)?.toInt() ?: ReminderScheduleConfig.DEFAULT_INTERVAL
        val mask = (data?.get("${prefix}DaysMask") as? Number)?.toInt() ?: ReminderScheduleConfig.defaultDaysMask()
        return ScheduleProfile(name, interval, ReminderScheduleConfig.maskToDays(mask))
    }

    fun scheduleProfiles(data: Map<String, Any?>?): ScheduleProfilesState {
        val idx = (data?.get("scheduleProfileActive") as? Number)?.toInt()?.coerceIn(0, 1) ?: 0
        return ScheduleProfilesState(
            scheduleProfile(data, "profile1", "Perfil 1"),
            scheduleProfile(data, "profile2", "Perfil 2"),
            idx,
        )
    }

    fun scheduleProfilesToMap(state: ScheduleProfilesState): Map<String, Any> {
        fun profileMap(prefix: String, profile: ScheduleProfile) = mapOf(
            "${prefix}Name" to profile.nombre,
            "${prefix}Interval" to profile.intervalMin,
            "${prefix}DaysMask" to ReminderScheduleConfig.daysToMask(profile.activeDays),
        )
        return profileMap("profile1", state.perfil1) + profileMap("profile2", state.perfil2) +
            mapOf("scheduleProfileActive" to state.indiceActivo)
    }

    fun apariencia(data: Map<String, Any?>?): AparienciaApp {
        val tema = runCatching { TemaApp.valueOf(data?.get("temaApp") as? String ?: TemaApp.SISTEMA.name) }
            .getOrDefault(TemaApp.SISTEMA)
        val escala = runCatching { EscalaTexto.valueOf(data?.get("escalaTexto") as? String ?: EscalaTexto.NORMAL.name) }
            .getOrDefault(EscalaTexto.NORMAL)
        return AparienciaApp(tema, escala)
    }

    fun aparienciaToMap(apariencia: AparienciaApp): Map<String, Any> = mapOf(
        "temaApp" to apariencia.tema.name,
        "escalaTexto" to apariencia.escalaTexto.name,
    )

    fun hidratacion(data: Map<String, Any?>?): ConfigHidratacion = ConfigHidratacion(
        activo = data?.get("hydrationActive") as? Boolean ?: false,
        intervaloMin = (data?.get("hydrationIntervalMin") as? Number)?.toInt() ?: 60,
    )

    fun hidratacionToMap(config: ConfigHidratacion): Map<String, Any> = mapOf(
        "hydrationActive" to config.activo,
        "hydrationIntervalMin" to config.intervaloMin,
    )

    fun pauseRecordToMap(r: PauseRecord): Map<String, Any> =
        mapOf("timestamp" to r.timestamp, "type" to r.type.name, "status" to r.status.name, "exerciseName" to r.exerciseName)

    fun pauseRecord(id: String, d: Map<String, Any?>): PauseRecord = PauseRecord(
        timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
        type = PauseType.fromName(d["type"] as? String),
        status = runCatching { PauseStatus.valueOf(d["status"] as String) }.getOrDefault(PauseStatus.COMPLETADA),
        exerciseName = d["exerciseName"] as? String ?: "",
        id = id,
    )

    fun walkToMap(w: WalkSession): Map<String, Any> = mapOf("timestamp" to w.timestamp, "steps" to w.steps, "km" to w.km)

    fun walk(id: String, d: Map<String, Any?>): WalkSession = WalkSession(
        timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
        steps = (d["steps"] as? Number)?.toInt() ?: 0,
        km = (d["km"] as? Number)?.toDouble() ?: 0.0,
        id = id,
    )

    fun routineToMap(r: AiRoutine): Map<String, Any> = mapOf(
        "timestamp" to r.timestamp,
        "exercises" to r.exercises.map {
            mapOf("name" to it.name, "category" to it.category, "sets" to it.sets, "reps" to it.reps, "instructions" to it.instructions)
        },
    )

    @Suppress("UNCHECKED_CAST")
    fun routine(id: String, d: Map<String, Any?>): AiRoutine = AiRoutine(
        timestamp = (d["timestamp"] as? Number)?.toLong() ?: 0L,
        exercises = (d["exercises"] as? List<Map<String, Any?>>).orEmpty().map {
            AiExercise(
                name = it["name"] as? String ?: "",
                category = it["category"] as? String ?: "general",
                sets = (it["sets"] as? Number)?.toInt() ?: 1,
                reps = (it["reps"] as? Number)?.toInt() ?: 10,
                instructions = it["instructions"] as? String ?: "",
            )
        },
        id = id,
    )
}
