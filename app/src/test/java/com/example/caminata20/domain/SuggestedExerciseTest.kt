package com.example.caminata20.domain

import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.usecase.SuggestedExercise
import com.example.caminata20.fakes.FakeLocalState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/** HU-16: el ejercicio sugerido se elige al azar, se persiste y cambia con "Otro ejercicio". */
class SuggestedExerciseTest {
    @Test fun `sin ejercicio valido elige uno del tipo y lo guarda`() = runTest {
        val local = FakeLocalState()
        local.lastExercise.value = "est_01"
        val ex = SuggestedExercise.ensure(local, PauseType.RESPIRACION)
        assertEquals(PauseType.RESPIRACION, ex.type)
        assertEquals(ex.id, local.lastExercise.value)
    }

    @Test fun `ensure conserva el ejercicio valido`() = runTest {
        val local = FakeLocalState()
        local.lastExercise.value = "est_05"
        assertEquals("est_05", SuggestedExercise.ensure(local, PauseType.ESTIRAMIENTO).id)
    }

    @Test fun `shuffle cambia a otro del mismo tipo`() = runTest {
        val local = FakeLocalState()
        local.lastExercise.value = "est_01"
        repeat(20) {
            val antes = local.lastExercise.value
            val ex = SuggestedExercise.shuffle(local, PauseType.ESTIRAMIENTO)
            assertEquals(PauseType.ESTIRAMIENTO, ex.type)
            assertNotEquals(antes, ex.id)
        }
    }
}
