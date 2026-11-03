package com.example.caminata20.data

import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.ExerciseVisual
import com.example.caminata20.domain.model.PauseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** HU-16: catálogo local, instrucciones y gráfico explicativo por ejercicio. */
class ExerciseCatalogTest {
    @Test fun `al menos 12 ejercicios por cada tipo de pausa`() {
        PauseType.entries.forEach { assertTrue(it.name, ExerciseCatalog.byType(it).size >= 12) }
    }

    @Test fun `cada ejercicio tiene nombre, instrucciones y duracion`() {
        ExerciseCatalog.all.forEach {
            assertTrue(it.name.isNotBlank()); assertTrue(it.steps.isNotEmpty()); assertTrue(it.durationSec > 0)
            assertTrue(it.instructions.isNotBlank())
        }
        assertEquals(ExerciseCatalog.all.size, ExerciseCatalog.all.map { it.id }.toSet().size)
    }

    @Test fun `cada ejercicio tiene grafico coherente con su tipo`() {
        val est = ExerciseCatalog.byId("est_01")!!
        assertEquals(ExerciseVisual.NECK_TILT, ExerciseCatalog.visualFor(est))
        ExerciseCatalog.byType(PauseType.DESCANSO_VISUAL).forEach {
            assertTrue(ExerciseCatalog.visualFor(it) in setOf(ExerciseVisual.EYE_DISTANCE, ExerciseVisual.EYE_MOVE, ExerciseVisual.EYES_CLOSED))
        }
        assertEquals(ExerciseVisual.WATER, ExerciseCatalog.visualFor(ExerciseCatalog.byId("hid_01")!!))
        assertEquals(ExerciseVisual.BREATH, ExerciseCatalog.visualFor(ExerciseCatalog.byId("res_01")!!))
    }

    @Test fun `estiramiento incluye 10 basicos con stickman propio`() {
        val basicos = (13..22).map { ExerciseCatalog.byId("est_%02d".format(it))!! }
        basicos.forEach { assertEquals(PauseType.ESTIRAMIENTO, it.type); assertTrue(it.steps.size in 3..4) }
        val visuales = basicos.map(ExerciseCatalog::visualFor)
        assertEquals(10, visuales.toSet().size)
        val antiguos = setOf(ExerciseVisual.NECK_TILT, ExerciseVisual.SHOULDER_ROLL, ExerciseVisual.ARM_STRETCH, ExerciseVisual.SIDE_BEND,
            ExerciseVisual.TORSO_TWIST, ExerciseVisual.FORWARD_BEND, ExerciseVisual.LEG_STRETCH)
        assertTrue(visuales.none { it in antiguos })
        assertEquals(22, ExerciseCatalog.byType(PauseType.ESTIRAMIENTO).size)
        assertEquals(58, ExerciseCatalog.all.size)
    }

    @Test fun `todos los ejercicios tienen un grafico asignado`() {
        ExerciseCatalog.all.forEach { ExerciseCatalog.visualFor(it) }
        val usados = ExerciseCatalog.all.map(ExerciseCatalog::visualFor).toSet()
        assertTrue(usados.containsAll(listOf(ExerciseVisual.TRICEPS, ExerciseVisual.QUAD_STAND, ExerciseVisual.HEEL_RAISE)))
    }

    @Test fun `next rota dentro del tipo elegido`() {
        val a = ExerciseCatalog.next(PauseType.RESPIRACION, null)
        val b = ExerciseCatalog.next(PauseType.RESPIRACION, a.id)
        assertEquals(PauseType.RESPIRACION, b.type); assertNotEquals(a.id, b.id)
    }
}
