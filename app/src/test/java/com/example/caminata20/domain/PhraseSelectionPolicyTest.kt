package com.example.caminata20.domain

import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.model.PhraseCategory
import com.example.caminata20.domain.model.PhraseState
import com.example.caminata20.domain.policy.PhraseSelectionPolicy
import org.junit.Assert.assertEquals
import org.junit.Test

/** HU-07 CA-02: no repetir la última frase dentro de las 24 h. */
class PhraseSelectionPolicyTest {
    private val policy = PhraseSelectionPolicy()
    private val now = 1_700_000_000_000L
    private val day = 24 * 60 * 60 * 1000L

    private val frases = listOf(
        Phrase("p01", "Uno", PhraseCategory.MOTIVACIONAL),
        Phrase("p02", "Dos", PhraseCategory.ERGONOMICA),
        Phrase("p03", "Tres", PhraseCategory.MOTIVACIONAL),
    )

    @Test fun `excluye la ultima frase si se mostro hace menos de 24 horas`() {
        val result = policy.candidates(frases, PhraseState("p02", now - day + 1), now)
        assertEquals(listOf("p01", "p03"), result.map { it.id })
    }

    @Test fun `permite repetir la ultima frase despues de 24 horas`() {
        val result = policy.candidates(frases, PhraseState("p02", now - day), now)
        assertEquals(frases, result)
    }

    @Test fun `sin estado devuelve todas las frases`() {
        assertEquals(frases, policy.candidates(frases, PhraseState(null, null), now))
    }

    @Test fun `catalogo de una sola frase devuelve esa frase`() {
        val solo = listOf(frases.first())
        assertEquals(solo, policy.candidates(solo, PhraseState("p01", now - 1_000), now))
    }
}
