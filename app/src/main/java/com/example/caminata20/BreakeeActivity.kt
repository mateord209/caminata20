package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import com.google.firebase.auth.FirebaseAuth
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.caminata20.ui.BreakeeApp
import com.example.caminata20.ui.theme.BreakeeTheme

/** Host Compose (single-activity) de las pantallas Breakee. Se abre tras el login de Firebase. */
@AndroidEntryPoint
class BreakeeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Si se abre desde una notificación sin sesión, se exige el login de Firebase
        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        enableEdgeToEdge()
        setContent {
            BreakeeTheme { BreakeeApp(startRoute = intent.getStringExtra(EXTRA_ROUTE)) }
        }
    }

    companion object {
        const val EXTRA_ROUTE = "extra_route"
    }
}
