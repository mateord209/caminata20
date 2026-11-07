package com.example.caminata20.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** HU-07 CA-01: la frase aparece en la notificación sin perder las instrucciones. */
class NotificationContentBuilderTest {
    private val content = buildPauseNotificationContent(
        typeEmoji = "🤸",
        typeTitle = "¡Estiramiento muscular!",
        exerciseName = "Estiramiento de cuello",
        instructions = "Inclina la cabeza hacia el hombro derecho 15 s.",
        durationLabel = "2 min",
        area = "Cuello y hombros",
        phrase = "Tu cuerpo agradece cada pausa.",
    )

    @Test fun `la frase aparece en el subtexto y en el big text`() {
        assertEquals("Tu cuerpo agradece cada pausa.", content.subText)
        assertTrue(content.bigText.contains("💡 Tu cuerpo agradece cada pausa."))
    }

    @Test fun `conserva las instrucciones, la duracion y el area del ejercicio`() {
        assertTrue(content.bigText.contains("Inclina la cabeza hacia el hombro derecho 15 s."))
        assertTrue(content.bigText.contains("2 min · Cuello y hombros"))
        assertEquals("Estiramiento de cuello · 2 min", content.text)
        assertEquals("🤸 ¡Estiramiento muscular! — Estiramiento de cuello", content.bigTitle)
    }
}
