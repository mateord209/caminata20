package com.example.caminata20.data.repository

import com.example.caminata20.data.local.PhraseCatalog
import com.example.caminata20.domain.model.Phrase
import com.example.caminata20.domain.policy.PhraseSelectionPolicy
import com.example.caminata20.domain.repository.PhraseRepository
import com.example.caminata20.domain.repository.PhraseStateStore
import com.example.caminata20.domain.usecase.Clock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/** HU-07: catálogo local + estado persistido; elige sin repetir la última frase en 24 h. */
@Singleton
class LocalPhraseRepository @Inject constructor(
    private val store: PhraseStateStore,
    private val policy: PhraseSelectionPolicy,
    private val clock: Clock,
    private val random: Random,
) : PhraseRepository {

    override fun current(): Flow<Phrase> = store.state().map { state ->
        PhraseCatalog.all.firstOrNull { it.id == state.lastId } ?: PhraseCatalog.all.first()
    }

    override suspend fun next(): Phrase {
        val state = store.state().first()
        val now = clock.now()
        val candidates = policy.candidates(PhraseCatalog.all, state, now)
        val phrase = candidates[random.nextInt(candidates.size)]
        store.save(phrase.id, now)
        return phrase
    }

    override suspend fun clear() = store.clear()
}
