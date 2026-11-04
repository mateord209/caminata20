package com.example.caminata20.domain

import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.usecase.RachaDiariaUseCase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

/** HU-12/HU-17: la racha respeta la meta diaria, el día en curso y el Modo reunión. */
class RachaDiariaUseCaseTest {
    private val todos = (1..7).toSet()
    private val ahora: Long = Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 3, 15, 0, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun inicioDia(offset: Int): Long = Calendar.getInstance().apply {
        timeInMillis = ahora
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        add(Calendar.DAY_OF_YEAR, -offset)
    }.timeInMillis

    private fun pausas(offset: Int, n: Int) = (0 until n).map {
        PauseRecord(inicioDia(offset) + 9 * 3_600_000L + it * 60_000L, PauseType.ESTIRAMIENTO, PauseStatus.COMPLETADA, "")
    }

    @Test fun `dia en curso sin meta no reinicia la racha`() {
        val r = pausas(0, 1) + pausas(1, 3) + pausas(2, 3)
        assertEquals(2, RachaDiariaUseCase.calcular(r, 3, todos, ahora))
    }

    @Test fun `dia en curso con meta suma`() {
        val r = pausas(0, 3) + pausas(1, 3)
        assertEquals(2, RachaDiariaUseCase.calcular(r, 3, todos, ahora))
    }

    @Test fun `dia pasado sin meta rompe la racha`() {
        val r = pausas(1, 3) + pausas(2, 1) + pausas(3, 3)
        assertEquals(1, RachaDiariaUseCase.calcular(r, 3, todos, ahora))
    }

    @Test fun `dia con modo reunion no rompe la racha`() {
        val r = pausas(1, 3) + pausas(2, 1) + pausas(3, 3)
        assertEquals(2, RachaDiariaUseCase.calcular(r, 3, todos, ahora, diasReunion = setOf(inicioDia(2))))
    }

    @Test fun `la meta configurada cambia la racha`() {
        val r = pausas(1, 5) + pausas(2, 5)
        assertEquals(2, RachaDiariaUseCase.calcular(r, 5, todos, ahora))
        assertEquals(0, RachaDiariaUseCase.calcular(r, 6, todos, ahora))
    }
}
