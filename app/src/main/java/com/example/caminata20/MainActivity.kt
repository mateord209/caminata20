package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.caminata20.domain.repository.AuthRepository
import com.example.caminata20.ui.BreakeeApp
import com.example.caminata20.ui.theme.BreakeeTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/** Única Activity de la app: aloja el grafo de Navigation Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var auth: AuthRepository

    private var deepLink by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        deepLink = intent.getStringExtra(EXTRA_ROUTE)
        setContent {
            BreakeeTheme {
                BreakeeApp(deepLink = deepLink, isSignedIn = { auth.currentUserOrNull() != null })
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
