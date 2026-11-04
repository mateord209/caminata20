package com.example.caminata20.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.caminata20.domain.model.PhraseState
import com.example.caminata20.domain.repository.PhraseStateStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/** HU-07: última frase mostrada en el DataStore existente. */
@Singleton
class DataStorePhraseStateStore @Inject constructor(
    @ApplicationContext private val context: Context,
) : PhraseStateStore {
    private val store get() = context.breakeeDataStore

    override fun state(): Flow<PhraseState> = store.data.map { prefs ->
        PhraseState(prefs[Keys.LAST_ID], prefs[Keys.LAST_AT])
    }

    override suspend fun save(id: String, at: Long) {
        store.edit {
            it[Keys.LAST_ID] = id
            it[Keys.LAST_AT] = at
        }
    }

    override suspend fun clear() {
        store.edit {
            it.remove(Keys.LAST_ID)
            it.remove(Keys.LAST_AT)
        }
    }

    private object Keys {
        val LAST_ID = stringPreferencesKey("phrase_last_id")
        val LAST_AT = longPreferencesKey("phrase_last_at")
    }
}
