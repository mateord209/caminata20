package com.example.caminata20.domain

import com.example.caminata20.domain.usecase.DiaCumplimiento
import com.example.caminata20.domain.usecase.WeeklyShareSummary
import org.junit.Assert.assertTrue
import org.junit.Test

class WeeklyShareSummaryTest {
    @Test
    fun `incluye total y porcentaje semanal agregado sin promediar dias`() {
        val dias = listOf(
            DiaCumplimiento("Lunes", 8, 8),
            DiaCumplimiento("Martes", 8, 8),
            DiaCumplimiento("Miércoles", 8, 8),
            DiaCumplimiento("Jueves", 8, 8),
            DiaCumplimiento("Viernes", 8, 8),
            DiaCumplimiento("Sábado", 0, 8),
            DiaCumplimiento("Domingo", 0, 8),
        )

        val texto = WeeklyShareSummary.generar(dias, racha = 3, metaDiaria = 8)

        assertTrue(texto.contains("40 de 56 pausas"))
        assertTrue(texto.contains("71%"))
        assertTrue(texto.contains("Racha: 3 días"))
        assertTrue(texto.contains("Meta diaria: 8 pausas"))
    }

    @Test
    fun `no promedia porcentajes de dias sin pausas esperadas`() {
        // El domingo inactivo tiene porcentaje diario 100; promediarlo subiría el total global.
        val dias = listOf(
            DiaCumplimiento("Domingo", 0, 0),
            DiaCumplimiento("Lunes", 0, 8),
        )

        val texto = WeeklyShareSummary.generar(dias, racha = 0, metaDiaria = 8)

        assertTrue(texto.contains("0 de 8 pausas · 0%"))
    }

    @Test
    fun `sin pausas esperadas devuelve cero sin excepcion`() {
        val dias = List(7) { DiaCumplimiento("Día $it", 0, 0) }

        val texto = WeeklyShareSummary.generar(dias, racha = 0, metaDiaria = 8)

        assertTrue(texto.contains("0 de 0 pausas · 0%"))
    }

    @Test
    fun `un dia sin pausas aparece en cero por ciento`() {
        val texto = WeeklyShareSummary.generar(listOf(DiaCumplimiento("Lunes", 0, 8)), racha = 0, metaDiaria = 8)

        assertTrue(texto.contains("Lunes: 0/8 (0%)"))
    }
}
