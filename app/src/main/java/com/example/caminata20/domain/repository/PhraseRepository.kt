package com.example.caminata20.domain.repository

import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.model.PhraseState
import kotlinx.coroutines.flow.Flow

/** HU-07: frase vigente y rotación de frases para Inicio y notificaciones. */
interface PhraseRepository {
    fun current(): Flow<Phrase>
    suspend fun next(): Phrase
    suspend fun clear()
}

/** HU-07: persistencia de la última frase mostrada, sin depender de la capa data. */
interface PhraseStateStore {
    fun state(): Flow<PhraseState>
    suspend fun save(id: String, at: Long)
    suspend fun clear()
}
