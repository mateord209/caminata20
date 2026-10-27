package com.example.caminata20.data

import com.example.caminata20.data.system.AlarmReminderScheduler
import com.example.caminata20.domain.model.ReminderTime
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AlarmReminderSchedulerTest {
    private fun at(h: Int, m: Int, dayOffset: Int = 0) = Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 26 + dayOffset, h, m, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    @Test
    fun `programa hoy si la hora no paso y manana si ya paso`() {
        val now = at(10, 0)
        assertEquals(at(18, 30), AlarmReminderScheduler.nextTrigger(ReminderTime(18, 30), now))
        assertEquals(at(9, 0, 1), AlarmReminderScheduler.nextTrigger(ReminderTime(9, 0), now))
    }
}
