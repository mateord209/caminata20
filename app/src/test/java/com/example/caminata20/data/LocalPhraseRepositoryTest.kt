package com.example.caminata20.data

import com.example.caminata20.data.local.PhraseCatalog
import com.example.caminata20.data.repository.LocalPhraseRepository
import com.example.caminata20.domain.model.PhraseState
import com.example.caminata20.domain.policy.PhraseSelectionPolicy
import com.example.caminata20.domain.usecase.Clock
import com.example.caminata20.fakes.FakePhraseStateStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/** HU-07: selección persistente sin repetir la última frase en 24 h. */
class LocalPhraseRepositoryTest {
    private val now = 1_700_000_000_000L
    private val clock = Clock { now }

    private fun repository(store: FakePhraseStateStore): LocalPhraseRepository =
        LocalPhraseRepository(store, PhraseSelectionPolicy(), clock, Random(7))

    @Test fun `next no repite la ultima frase mostrada`() = runTest {
        val store = FakePhraseStateStore(PhraseState("p05", now - 60_000))
        val phrase = repository(store).next()
        assertNotEquals("p05", phrase.id)
        assertTrue(PhraseCatalog.all.any { it.id == phrase.id })
    }

    @Test fun `next persiste el id y la hora de la frase elegida`() = runTest {
        val store = FakePhraseStateStore(PhraseState("p05", now - 60_000))
        val phrase = repository(store).next()
        assertEquals(phrase.id, store.stateFlow.value.lastId)
        assertEquals(now, store.stateFlow.value.lastShownAt)
    }

    @Test fun `current devuelve la frase guardada`() = runTest {
        val store = FakePhraseStateStore(PhraseState("p12", now))
        assertEquals("p12", repository(store).current().first().id)
    }

    @Test fun `current devuelve la primera frase si no hay estado`() = runTest {
        val store = FakePhraseStateStore(PhraseState(null, null))
        assertEquals(PhraseCatalog.all.first(), repository(store).current().first())
    }

    @Test fun `clear borra el estado persistido`() = runTest {
        val store = FakePhraseStateStore(PhraseState("p02", now))
        repository(store).clear()
        assertEquals(PhraseState(null, null), store.stateFlow.value)
    }
}
