package com.example.caminata20

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.caminata20.ui.BreakeeApp
import com.example.caminata20.ui.theme.BreakeeTheme

/** Host Compose (single-activity) de las pantallas Breakee. Se abre tras el login de Firebase. */
class BreakeeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreakeeTheme { BreakeeApp(startRoute = intent.getStringExtra(EXTRA_ROUTE)) }
        }
    }

    companion object {
        const val EXTRA_ROUTE = "extra_route"
    }
}
