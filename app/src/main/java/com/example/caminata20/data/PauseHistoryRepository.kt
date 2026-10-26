package com.example.caminata20.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar

enum class PauseStatus { COMPLETADA, POSPUESTA }

data class PauseRecord(val timestamp: Long, val type: PauseType, val status: PauseStatus, val exerciseName: String = "")

/**
 * Historial local MÍNIMO de pausas (JSON en DataStore).
 * TODO(HU-04): el historial completo (filtros, borrado, estadísticas, persistencia definitiva) lo
 * implementa otro integrante; reemplazar esta clase por su repositorio respetando esta interfaz.
 */
class PauseHistoryRepository(private val context: Context) {
    private val key = stringPreferencesKey("pause_history_json")

    val records: Flow<List<PauseRecord>> = context.breakeeDataStore.data.map { decode(it[key]) }

    val today: Flow<List<PauseRecord>> = records.map { list -> list.filter { isToday(it.timestamp) } }

    suspend fun add(record: PauseRecord) {
        context.breakeeDataStore.edit { prefs ->
            val list = decode(prefs[key]) + record
            prefs[key] = encode(list.takeLast(MAX))
        }
    }

    private fun encode(list: List<PauseRecord>): String = JSONArray().apply {
        list.forEach {
            put(JSONObject().put("ts", it.timestamp).put("type", it.type.name).put("status", it.status.name).put("ex", it.exerciseName))
        }
    }.toString()

    private fun decode(json: String?): List<PauseRecord> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PauseRecord(
                    o.getLong("ts"), PauseType.fromName(o.optString("type")),
                    runCatching { PauseStatus.valueOf(o.optString("status")) }.getOrDefault(PauseStatus.COMPLETADA),
                    o.optString("ex"),
                )
            }
        }.getOrDefault(emptyList())
    }

    companion object {
        private const val MAX = 500

        fun isToday(ts: Long): Boolean {
            val a = Calendar.getInstance()
            val b = Calendar.getInstance().apply { timeInMillis = ts }
            return a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)
        }
    }
}
