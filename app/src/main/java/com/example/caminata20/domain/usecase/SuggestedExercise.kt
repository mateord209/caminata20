package com.example.caminata20.domain.usecase

import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.model.Exercise
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.domain.repository.LocalStateRepository
import kotlinx.coroutines.flow.first

/**
 * HU-16: ejercicio sugerido compartido (Mi Día, Alertas, notificación y guía).
 * Se persiste en [LocalStateRepository.lastExerciseId] para que todas las pantallas coincidan.
 */
object SuggestedExercise {
    /** Ejercicio guardado si es del tipo actual; si no, null (hay que elegir uno). */
    fun current(lastId: String?, type: PauseType): Exercise? =
        ExerciseCatalog.byId(lastId)?.takeIf { it.type == type }

    /** Si no hay ejercicio válido para el tipo (primer uso o cambio de tipo), elige uno al azar y lo guarda. */
    suspend fun ensure(local: LocalStateRepository, type: PauseType): Exercise {
        val lastId = local.lastExerciseId.first()
        current(lastId, type)?.let { return it }
        return ExerciseCatalog.next(type, lastId).also { local.setLastExercise(it.id) }
    }

    /** "Otro ejercicio": cambia por uno distinto del mismo tipo y lo guarda. */
    suspend fun shuffle(local: LocalStateRepository, type: PauseType): Exercise {
        val lastId = local.lastExerciseId.first()
        return ExerciseCatalog.next(type, lastId).also { local.setLastExercise(it.id) }
    }
}
