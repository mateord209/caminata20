package com.example.caminata20.domain

import com.example.caminata20.domain.model.EscalaTexto
import org.junit.Assert.assertEquals
import org.junit.Test

/** HU-19: las tres escalas de accesibilidad equivalen a 100 %, 120 % y 140 %. */
class EscalaTextoTest {
    @Test
    fun `las escalas exponen los factores 100 120 y 140`() {
        assertEquals(
            listOf(1f, 1.2f, 1.4f),
            EscalaTexto.entries.map { it.factor },
        )
    }
}
