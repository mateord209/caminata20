package com.example.caminata20

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalkActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1
    private var currentSteps = 0

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null
    private var totalDistanceMeters = 0.0

    private var isWalking = false

    private lateinit var tvSteps: TextView
    private lateinit var tvDistance: TextView
    private lateinit var btnToggleWalk: Button

    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY, 3000L
    ).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return

            // Ignorar lecturas poco precisas (ruido de GPS)
            if (location.hasAccuracy() && location.accuracy > 15f) return

            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                // Ignorar movimientos muy chicos (ruido estando quieto)
                if (distance > 5f) {
                    totalDistanceMeters += distance
                    updateDistanceUI()
                    lastLocation = location
                }
            } else {
                lastLocation = location
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_walk)

        tvSteps = findViewById(R.id.tvSteps)
        tvDistance = findViewById(R.id.tvDistance)
        btnToggleWalk = findViewById(R.id.btnToggleWalk)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        btnToggleWalk.setOnClickListener {
            if (!isWalking) startWalk() else stopWalk()
        }
    }

    @SuppressLint("MissingPermission")
    private fun startWalk() {
        if (!hasPermissions()) {
            requestPermissions()
            return
        }

        initialSteps = -1
        currentSteps = 0
        totalDistanceMeters = 0.0
        lastLocation = null
        tvSteps.text = "0"
        tvDistance.text = "0.00"

        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)

        isWalking = true
        btnToggleWalk.text = "Finalizar caminata"
        btnToggleWalk.setBackgroundColor(0xFFF44336.toInt())
    }

    private fun stopWalk() {
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isWalking = false
        btnToggleWalk.text = "Iniciar caminata"
        btnToggleWalk.setBackgroundColor(0xFF4CAF50.toInt())

        saveSession(currentSteps, totalDistanceMeters / 1000.0)
        showResultMessage(currentSteps, totalDistanceMeters / 1000.0)
    }

    private fun updateDistanceUI() {
        val km = totalDistanceMeters / 1000.0
        tvDistance.text = String.format("%.2f", km)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps < 0) {
                initialSteps = event.values[0].toInt()
            }
            currentSteps = event.values[0].toInt() - initialSteps
            tvSteps.text = currentSteps.toString()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun hasPermissions(): Boolean {
        val locationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val activityRecognitionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        return locationGranted && activityRecognitionGranted
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }
        ActivityCompat.requestPermissions(this, permissions.toTypedArray(), 200)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 200 && hasPermissions()) {
            startWalk()
        } else {
            Toast.makeText(this, "Se necesitan los permisos para contar pasos y distancia", Toast.LENGTH_LONG).show()
        }
    }

    private fun showResultMessage(steps: Int, km: Double) {
        val mensaje = if (steps >= 1000 || km >= 0.8) {
            "¡Felicitaciones! Completaste $steps pasos (${String.format("%.2f", km)} km) 🎉"
        } else {
            "Caminaste $steps pasos (${String.format("%.2f", km)} km). ¡Intentá caminar un poco más la próxima vez!"
        }
        Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show()
    }

    private fun saveSession(steps: Int, km: Double) {
        val prefs: SharedPreferences = getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        val totalSteps = prefs.getInt("total_steps", 0) + steps
        val totalKm = prefs.getFloat("total_km", 0f) + km.toFloat()
        val weekKm = prefs.getFloat("week_km", 0f) + km.toFloat()

        // Guardar el historial de sesiones individuales
        val sessionsJson = prefs.getString("sessions", "[]")
        val sessionsArray = JSONArray(sessionsJson)

        val newSession = JSONObject()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        newSession.put("date", dateFormat.format(Date()))
        newSession.put("steps", steps)
        newSession.put("km", km)
        sessionsArray.put(newSession)

        prefs.edit()
            .putInt("total_steps", totalSteps)
            .putFloat("total_km", totalKm)
            .putFloat("week_km", weekKm)
            .putString("sessions", sessionsArray.toString())
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isWalking) {
            sensorManager.unregisterListener(this)
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
}