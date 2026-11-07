package com.example.caminata20.domain.model

/** HU-07: frase motivacional o tip ergonómico. */
data class Phrase(
    val id: String,
    val text: String,
    val category: PhraseCategory,
)

enum class PhraseCategory { MOTIVACIONAL, ERGONOMICA }

/** HU-07: última frase mostrada y su marca de tiempo (para evitar repetirla en 24 h). */
data class PhraseState(val lastId: String?, val lastShownAt: Long?)
