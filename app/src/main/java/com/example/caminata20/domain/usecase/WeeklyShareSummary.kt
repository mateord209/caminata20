package com.example.caminata20.domain.usecase

/**
 * HU-18: genera el resumen semanal compartible en texto plano.
 *
 * El porcentaje global se calcula sobre los totales de la semana
 * (`completadas / esperadas`), **no** como promedio de los porcentajes diarios:
 * los días sin pausas esperadas devuelven 100 % y, al promediarlos, inflarían el resultado.
 */
object WeeklyShareSummary {
    fun generar(dias: List<DiaCumplimiento>, racha: Int, metaDiaria: Int): String {
        val hechas = dias.sumOf { it.completadas }
        val esperadas = dias.sumOf { it.esperadas }
        val pct = if (esperadas == 0) 0 else (hechas * 100 / esperadas).coerceIn(0, 100)
        return buildString {
            appendLine("Mi semana con Breakee 🌿")
            appendLine("✅ $hechas de $esperadas pausas · $pct%")
            appendLine("🔥 Racha: $racha días")
            appendLine("🎯 Meta diaria: $metaDiaria pausas")
            appendLine()
            dias.forEach { appendLine("${it.etiqueta}: ${it.completadas}/${it.esperadas} (${it.porcentaje}%)") }
        }.trimEnd()
    }
}
