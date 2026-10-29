package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.data.migration.LegacyDataMigrator
import com.example.caminata20.di.ApplicationScope
import com.example.caminata20.domain.model.AparienciaApp
import com.example.caminata20.domain.model.TemaApp
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.ui.BreakeeApp
import com.example.caminata20.ui.theme.BreakeeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Única Activity de la app: aloja el grafo de Navigation Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var auth: AuthRepository
    @Inject lateinit var prefs: PreferencesRepository
    @Inject lateinit var migrator: LegacyDataMigrator
    @Inject @ApplicationScope lateinit var appScope: CoroutineScope

    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLink = intent.getStringExtra(EXTRA_ROUTE)
        setContent {
            val apariencia by prefs.apariencia.collectAsStateWithLifecycle(initialValue = AparienciaApp())
            val dark = when (apariencia.tema) {
                TemaApp.CLARO -> false
                TemaApp.OSCURO -> true
                TemaApp.SISTEMA -> isSystemInDarkTheme()
            }
            BreakeeTheme(darkTheme = dark, textScale = apariencia.escalaTexto.factor) {
                BreakeeApp(
                    deepLink = deepLink,
                    isSignedIn = { auth.currentUserOrNull() != null },
                    onAuthenticated = { appScope.launch { runCatching { migrator.migrateIfNeeded() } } },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        deepLink = intent.getStringExtra(EXTRA_ROUTE)
    }

    companion object {
        const val EXTRA_ROUTE = "extra_route"
    }
}
