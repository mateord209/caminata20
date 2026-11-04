package com.example.caminata20.domain.policy

import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.model.PhraseState
import javax.inject.Inject

/**
 * HU-07 CA-02: la siguiente frase no puede ser igual a la última mostrada
 * si esa última se mostró hace menos de 24 h.
 */
class PhraseSelectionPolicy @Inject constructor() {
    fun candidates(
        all: List<Phrase>,
        state: PhraseState,
        now: Long,
        windowMs: Long = WINDOW_MS,
    ): List<Phrase> {
        if (all.size <= 1) return all
        val lastId = state.lastId ?: return all
        val lastShownAt = state.lastShownAt ?: return all
        if (now - lastShownAt >= windowMs) return all
        val filtered = all.filterNot { it.id == lastId }
        return filtered.ifEmpty { all }
    }

    companion object {
        /** 24 horas en milisegundos. */
        const val WINDOW_MS: Long = 24 * 60 * 60 * 1000L
    }
}
