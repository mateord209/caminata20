package com.example.caminata20.domain.model

/** HU-08: categorías de pausa activa. */
enum class PauseType(val title: String, val notificationTitle: String, val description: String, val emoji: String) {
    ESTIRAMIENTO("Estiramiento", "¡Estiramiento muscular!", "Relaja cuello, espalda y brazos. 2-3 min.", "🤸"),
    DESCANSO_VISUAL("Descanso visual 20-20-20", "¡Descanso visual! (20-20-20)", "Mira a 20 pies (6 m) durante 20 segundos.", "👁️"),
    RESPIRACION("Respiración guiada", "¡Respiración guiada!", "Inhalaciones y exhalaciones profundas. 1-2 min.", "🫁"),
    HIDRATACION("Hidratación", "¡Hora de hidratación!", "Bebe un vaso de agua y mantente fresco.", "💧");

    companion object {
        fun fromName(name: String?): PauseType = entries.firstOrNull { it.name == name } ?: ESTIRAMIENTO
    }
}
