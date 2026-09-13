package com.example.caminata20

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class WalkActivity : AppCompatActivity() {

    private lateinit var tvSteps: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnToggleWalk: Button

    private val uiHandler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            tvSteps.text = WalkTrackingService.steps.toString()
            tvDistance.text = "%.2f".format(WalkTrackingService.km)
            uiHandler.postDelayed(this, 1000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        tvSteps = findViewById(R.id.tvSteps)
        tvDistance = findViewById(R.id.tvDistance)
        btnToggleWalk = findViewById(R.id.btnToggleWalk)

        updateButtonState()

        btnToggleWalk.setOnClickListener {
            if (!WalkTrackingService.isTracking) {
                if (!hasPermissions()) {
                    requestPermissions()
                    return@setOnClickListener
                }
                startService(Intent(this, WalkTrackingService::class.java).apply {
                    action = WalkTrackingService.ACTION_START
                })
                updateButtonState()
            } else {
                startService(Intent(this, WalkTrackingService::class.java).apply {
                    action = WalkTrackingService.ACTION_STOP
                })
                Toast.makeText(this, "Caminata finalizada y guardada", Toast.LENGTH_LONG).show()
                Handler(Looper.getMainLooper()).postDelayed({
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }, 1500)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateButtonState()
        uiHandler.post(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        uiHandler.removeCallbacks(updateRunnable)
    }

    private fun updateButtonState() {
        if (WalkTrackingService.isTracking) {
            btnToggleWalk.text = "Finalizar caminata"
            btnToggleWalk.setBackgroundColor(0xFFF44336.toInt())
        } else {
            btnToggleWalk.text = "Iniciar caminata"
            btnToggleWalk.setBackgroundColor(0xFF4CAF50.toInt())
        }
    }

    private fun hasPermissions(): Boolean {
        val locationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
        } else true
        val notifGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
        return locationGranted && activityRecognitionGranted && notifGranted
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 200)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && hasPermissions()) {
            startService(Intent(this, WalkTrackingService::class.java).apply {
                action = WalkTrackingService.ACTION_START
            })
            updateButtonState()
        } else {
            Toast.makeText(this, "Se necesitan los permisos para contar pasos y distancia", Toast.LENGTH_LONG).show()
        }
    }
}