package com.example.caminata20.data.local

import kotlin.random.Random

/** HU-07: frases motivacionales y tips ergonómicos (≥15). */
object MotivationalPhrases {
    val frases: List<String> = listOf(
        "Tu cuerpo agradece cada pausa que te regalas.",
        "Moverte unos minutos mejora tu concentración para el resto del día.",
        "Respira hondo: el oxígeno es combustible para tu mente.",
        "Estira con calma; la postura correcta previene molestias.",
        "Beber agua regularmente mantiene tu energía estable.",
        "Mirar lejos de la pantalla descansa tus ojos en segundos.",
        "Una pausa ahora evita la fatiga acumulada al final del día.",
        "La constancia en pequeños hábitos construye grandes resultados.",
        "Ajusta la altura de tu silla: pies apoyados y espalda recta.",
        "Parpadea con frecuencia para lubricar tus ojos.",
        "Levantarte y caminar activa la circulación de piernas.",
        "Cada pausa completada es una victoria para tu bienestar.",
        "Tu meta diaria está a un estiramiento de distancia.",
        "El descanso activo no es perder tiempo: es invertir en ti.",
        "Mantén los hombros relajados lejos de las orejas.",
        "Alterna tareas intensas con micro-pausas para sostener el ritmo.",
        "Hidrátate antes de sentir sed: tu cerebro lo notará.",
    )

    fun siguiente(ultimoIndice: Int?): Pair<Int, String> {
        if (frases.size <= 1) return 0 to frases.first()
        var idx = Random.nextInt(frases.size)
        while (ultimoIndice != null && idx == ultimoIndice) idx = Random.nextInt(frases.size)
        return idx to frases[idx]
    }
}
