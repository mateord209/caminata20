package com.example.caminata20.data.local

import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.model.PhraseCategory

/** HU-07: catálogo local de frases motivacionales y tips ergonómicos (22). */
object PhraseCatalog {
    val all: List<Phrase> = listOf(
        Phrase("p01", "Tu cuerpo agradece cada pausa que te regalas.", PhraseCategory.MOTIVACIONAL),
        Phrase("p02", "Moverte unos minutos mejora tu concentración para el resto del día.", PhraseCategory.MOTIVACIONAL),
        Phrase("p03", "Respira hondo: el oxígeno es combustible para tu mente.", PhraseCategory.MOTIVACIONAL),
        Phrase("p04", "Estira con calma; la postura correcta previene molestias.", PhraseCategory.ERGONOMICA),
        Phrase("p05", "Beber agua regularmente mantiene tu energía estable.", PhraseCategory.MOTIVACIONAL),
        Phrase("p06", "Mirar lejos de la pantalla descansa tus ojos en segundos.", PhraseCategory.ERGONOMICA),
        Phrase("p07", "Una pausa ahora evita la fatiga acumulada al final del día.", PhraseCategory.MOTIVACIONAL),
        Phrase("p08", "La constancia en pequeños hábitos construye grandes resultados.", PhraseCategory.MOTIVACIONAL),
        Phrase("p09", "Ajusta la altura de tu silla: pies apoyados y espalda recta.", PhraseCategory.ERGONOMICA),
        Phrase("p10", "Parpadea con frecuencia para lubricar tus ojos.", PhraseCategory.ERGONOMICA),
        Phrase("p11", "Levantarte y caminar activa la circulación de piernas.", PhraseCategory.MOTIVACIONAL),
        Phrase("p12", "Cada pausa completada es una victoria para tu bienestar.", PhraseCategory.MOTIVACIONAL),
        Phrase("p13", "Tu meta diaria está a un estiramiento de distancia.", PhraseCategory.MOTIVACIONAL),
        Phrase("p14", "El descanso activo no es perder tiempo: es invertir en ti.", PhraseCategory.MOTIVACIONAL),
        Phrase("p15", "Mantén los hombros relajados lejos de las orejas.", PhraseCategory.ERGONOMICA),
        Phrase("p16", "Alterna tareas intensas con micro-pausas para sostener el ritmo.", PhraseCategory.MOTIVACIONAL),
        Phrase("p17", "Hidrátate antes de sentir sed: tu cerebro lo notará.", PhraseCategory.MOTIVACIONAL),
        Phrase("p18", "Apoya los pies en el piso y mantén las rodillas a 90 grados.", PhraseCategory.ERGONOMICA),
        Phrase("p19", "Ubica la pantalla a la altura de los ojos para no inclinar el cuello.", PhraseCategory.ERGONOMICA),
        Phrase("p20", "Usa una almohada lumbar para cuidar la curvatura de tu espalda.", PhraseCategory.ERGONOMICA),
        Phrase("p21", "Relaja las muñecas y mantén los codos cerca del cuerpo al teclear.", PhraseCategory.ERGONOMICA),
        Phrase("p22", "Haz micro-pausas de 20 segundos cada 20 minutos mirando a 6 metros.", PhraseCategory.ERGONOMICA),
    )
}
