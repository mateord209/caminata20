package com.example.caminata20.data

import com.example.caminata20.data.migration.LegacyParsers
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.Locale

class LegacyParsersTest {
    @Test
    fun `parsea caminatas legadas de SharedPreferences`() {
        Locale.setDefault(Locale.US)
        val json = """[{"date":"25/10/2026 08:30 AM","steps":1500,"km":1.2},{"date":"basura","steps":10,"km":0.01}]"""
        val list = LegacyParsers.walks(json, now = 99L)
        assertEquals(2, list.size)
        val cal = Calendar.getInstance().apply { timeInMillis = list[0].timestamp }
        assertEquals(25, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(8, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(1500, list[0].steps)
        assertEquals(99L, list[1].timestamp) // fecha ilegible → fallback
    }

    @Test
    fun `acepta formato a m p m en espanol`() {
        val ts = LegacyParsers.parseLegacyDate("25/10/2026 03:15 p. m.", 0L)
        assertNotEquals(0L, ts)
        assertEquals(15, Calendar.getInstance().apply { timeInMillis = ts }.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun `parsea rutinas IA e historial de pausas y tolera JSON invalido`() {
        val routines = LegacyParsers.routines(
            """[{"date":"01/01/2026 10:00 AM","exercises":[{"name":"Giro","category":"cuello","sets":2,"reps":8}]}]""", 0L,
        )
        assertEquals("Giro", routines.single().exercises.single().name)

        val history = LegacyParsers.pauseHistory("""[{"ts":5,"type":"RESPIRACION","status":"POSPUESTA","ex":"4-7-8"}]""")
        assertEquals(PauseType.RESPIRACION, history.single().type)
        assertEquals(PauseStatus.POSPUESTA, history.single().status)

        assertTrue(LegacyParsers.walks("{no es json", 0L).isEmpty())
        assertTrue(LegacyParsers.walks(null, 0L).isEmpty())
    }
}
