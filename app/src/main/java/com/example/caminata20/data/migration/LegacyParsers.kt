package com.example.caminata20.data.migration

import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.WalkSession
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Locale

/** Parseo puro de los JSON que la app guardaba en SharedPreferences/DataStore (testeable). */
object LegacyParsers {
    private const val LEGACY_DATE = "dd/MM/yyyy hh:mm a"

    /** Las fechas legadas eran texto con Locale del dispositivo; se prueba con varios locales. */
    fun parseLegacyDate(text: String?, fallback: Long): Long {
        if (text.isNullOrBlank()) return fallback
        val locales = listOf(Locale.getDefault(), Locale.forLanguageTag("es-CO"), Locale.US)
        for (loc in locales) {
            val normalized = text.replace("a. m.", "AM").replace("p. m.", "PM").replace("a.m.", "AM").replace("p.m.", "PM")
            for (candidate in listOf(text, normalized)) {
                runCatching { SimpleDateFormat(LEGACY_DATE, loc).apply { isLenient = false }.parse(candidate) }
                    .getOrNull()?.let { return it.time }
            }
        }
        return fallback
    }

    /** walk_stats.sessions: [{"date","steps","km"}] */
    fun walks(json: String?, now: Long): List<WalkSession> = array(json).map { o ->
        WalkSession(parseLegacyDate(o.optString("date"), now), o.optInt("steps", 0), o.optDouble("km", 0.0))
    }

    /** ai_exercises_stats.routines: [{"date","exercises":[{"name","category","sets","reps"}]}] */
    fun routines(json: String?, now: Long): List<AiRoutine> = array(json).map { o ->
        val exs = o.optJSONArray("exercises") ?: JSONArray()
        AiRoutine(
            timestamp = parseLegacyDate(o.optString("date"), now),
            exercises = (0 until exs.length()).map { i ->
                val e = exs.getJSONObject(i)
                AiExercise(e.optString("name"), e.optString("category", "general"), e.optInt("sets", 1), e.optInt("reps", 10))
            },
        )
    }

    /** DataStore pause_history_json (rama developRamirez anterior): [{"ts","type","status","ex"}] */
    fun pauseHistory(json: String?): List<PauseRecord> = array(json).map { o ->
        PauseRecord(
            timestamp = o.optLong("ts"),
            type = PauseType.fromName(o.optString("type")),
            status = runCatching { PauseStatus.valueOf(o.optString("status")) }.getOrDefault(PauseStatus.COMPLETADA),
            exerciseName = o.optString("ex"),
        )
    }

    private fun array(json: String?): List<org.json.JSONObject> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getJSONObject(it) }
        }.getOrDefault(emptyList())
    }
}
