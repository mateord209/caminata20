package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.ReminderScheduleConfig
import java.util.Calendar

/** Porcentaje diario de cumplimiento (HU-05). */
data class DiaCumplimiento(
    val etiqueta: String,
    val completadas: Int,
    val esperadas: Int,
) {
    val porcentaje: Int
        get() = if (esperadas <= 0) 100 else ((completadas.toFloat() / esperadas) * 100).toInt().coerceIn(0, 100)
}

object EstadisticasSemanalesUseCase {
    /** Últimos 7 días calendario (hoy incluido), de más antiguo a más reciente. */
    fun calcular(
        registros: List<PauseRecord>,
        metaDiaria: Int,
        diasActivos: Set<Int>,
        ahora: Long = System.currentTimeMillis(),
    ): List<DiaCumplimiento> {
        val cal = Calendar.getInstance().apply { timeInMillis = ahora }
        return (6 downTo 0).map { offset ->
            cal.timeInMillis = ahora
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            cal.add(Calendar.DAY_OF_YEAR, -offset)
            val inicio = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val fin = cal.timeInMillis
            val diaSemana = Calendar.getInstance().apply { timeInMillis = inicio }.get(Calendar.DAY_OF_WEEK)
            val completadas = registros.count {
                it.status == PauseStatus.COMPLETADA && it.timestamp in inicio until fin
            }
            val esperadas = if (diaSemana in diasActivos) metaDiaria.coerceAtLeast(1) else 0
            val etiqueta = when (offset) {
                0 -> "Hoy"
                1 -> "Ayer"
                else -> "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}"
            }
            DiaCumplimiento(etiqueta, completadas, esperadas)
        }
    }
}

/** Motor de racha diaria (HU-17). */
object RachaDiariaUseCase {
    fun calcular(
        registros: List<PauseRecord>,
        metaDiaria: Int,
        diasActivos: Set<Int>,
        ahora: Long = System.currentTimeMillis(),
    ): Int {
        var racha = 0
        val cal = Calendar.getInstance().apply { timeInMillis = ahora }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        // Revisar hacia atrás día a día
        repeat(365) {
            val inicio = cal.timeInMillis
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val diaSemana = Calendar.getInstance().apply { timeInMillis = inicio }.get(Calendar.DAY_OF_WEEK)
            if (diaSemana !in diasActivos) return@repeat // no rompe ni suma
            val fin = inicio + 86_400_000L
            val completadas = registros.count {
                it.status == PauseStatus.COMPLETADA && it.timestamp in inicio until fin
            }
            if (completadas >= metaDiaria) racha++ else return racha
        }
        return racha
    }
}

/** Esperadas hoy según meta (HU-05 CA-03 simplificado con meta diaria). */
fun metaEsperadaHoy(config: ReminderScheduleConfig, meta: Int, now: Long = System.currentTimeMillis()): Int =
    if (config.isActiveToday(now)) meta.coerceAtLeast(1) else 0
