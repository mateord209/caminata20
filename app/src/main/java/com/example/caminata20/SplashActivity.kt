package com.example.caminata20

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.caminata20.data.OnboardingRepository
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            // HU-20: onboarding solo la primera vez, antes del login
            lifecycleScope.launch {
                val done = OnboardingRepository(applicationContext).isDoneOnce()
                val next = if (done) LoginActivity::class.java else OnboardingActivity::class.java
                startActivity(Intent(this@SplashActivity, next))
                finish()
            }
        }, 2000)
    }
}