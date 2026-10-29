package com.example.caminata20.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/** DataStore único de la capa Breakee (Compose). Los datos legados siguen en SharedPreferences. */
val Context.breakeeDataStore: DataStore<Preferences> by preferencesDataStore(name = "breakee_prefs")
