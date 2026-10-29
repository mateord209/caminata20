package com.example.caminata20.data

import com.example.caminata20.data.remote.FirestoreMappers
import com.example.caminata20.domain.model.AiExercise
import com.example.caminata20.domain.model.AiRoutine
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.model.WalkSession
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FirestoreMappersTest {
    @Test
    fun `perfil usa el nombre de Firebase si no hay documento y limita la meta`() {
        val p = FirestoreMappers.profile(null, "Ana", "ana@x.com")
        assertEquals("Ana", p.name)
        assertEquals(8, p.dailyGoal)
        assertEquals(15, FirestoreMappers.profile(mapOf("dailyGoal" to 40L), null, null).dailyGoal)
        assertEquals(2, FirestoreMappers.profile(mapOf("dailyGoal" to 0L), null, null).dailyGoal)
    }

    @Test
    fun `preferencias y recordatorio`() {
        val s = FirestoreMappers.pauseSettings(mapOf("pauseType" to "RESPIRACION", "pauseDurationMin" to 3L))
        assertEquals(PauseType.RESPIRACION, s.type)
        assertEquals(3, s.durationMin)
        assertNull(FirestoreMappers.reminder(mapOf("reminderHour" to -1L)))
        assertEquals("07:05", FirestoreMappers.reminder(mapOf("reminderHour" to 7L, "reminderMinute" to 5L))!!.label)
        val schedule = FirestoreMappers.reminderSchedule(mapOf("intervalMin" to 45L, "activeDaysMask" to 31L))
        assertEquals(45, schedule.intervalMin)
        assertEquals(5, schedule.activeDays.size)
    }

    @Test
    fun `ida y vuelta de pausa, caminata y rutina`() {
        val r = PauseRecord(10L, PauseType.HIDRATACION, PauseStatus.POSPUESTA, "Vaso de agua")
        assertEquals(r.copy(id = "a"), FirestoreMappers.pauseRecord("a", FirestoreMappers.pauseRecordToMap(r)))

        val w = WalkSession(20L, 1234, 0.9)
        assertEquals(w.copy(id = "b"), FirestoreMappers.walk("b", FirestoreMappers.walkToMap(w)))

        val routine = AiRoutine(30L, listOf(AiExercise("Giro", "cuello", 2, 10, "Gira")))
        assertEquals(routine.copy(id = "c"), FirestoreMappers.routine("c", FirestoreMappers.routineToMap(routine)))
    }
}
