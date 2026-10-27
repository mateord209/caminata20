package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.caminata20.ui.onboarding.OnboardingScreen
import com.example.caminata20.ui.theme.BreakeeTheme

/** HU-20: onboarding (3 pantallas) que se muestra una sola vez, antes del login. */
@AndroidEntryPoint
class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BreakeeTheme {
                OnboardingScreen(onFinished = {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                })
            }
        }
    }
}
