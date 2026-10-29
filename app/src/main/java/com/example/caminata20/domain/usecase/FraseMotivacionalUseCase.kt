package com.example.caminata20.domain.usecase

import com.example.caminata20.data.local.MotivationalPhrases
import com.example.caminata20.domain.repository.LocalStateRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** HU-07: rota frases motivacionales cada cierto tiempo. */
@Singleton
class FraseMotivacionalUseCase @Inject constructor(
    private val local: LocalStateRepository,
) {
    suspend fun actualizarSiCorresponde(ahora: Long = System.currentTimeMillis()): String {
        val ultimoAt = local.lastPhraseAt() ?: 0L
        val ultimoIdx = local.lastPhraseIndex.first()
        val debeRotar = ultimoIdx == null || ahora - ultimoAt >= ROTACION_MS
        if (!debeRotar && ultimoIdx != null) return MotivationalPhrases.frases[ultimoIdx]
        val (idx, texto) = MotivationalPhrases.siguiente(ultimoIdx)
        local.setLastPhraseIndex(idx)
        local.setLastPhraseAt(ahora)
        return texto
    }

    companion object {
        private const val ROTACION_MS = 4 * 60 * 60 * 1000L
    }
}
