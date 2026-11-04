package com.example.caminata20.domain.usecase

import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.repository.PhraseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/** HU-07: frase vigente para Inicio y frase nueva para las notificaciones de pausa. */
@Singleton
class GetPhraseUseCase @Inject constructor(
    private val repository: PhraseRepository,
) {
    fun current(): Flow<Phrase> = repository.current()

    /** Elige una frase que no se haya mostrado en las últimas 24 h y la persiste. */
    suspend fun forNotification(): Phrase = repository.next()

    suspend fun clear() = repository.clear()
}
