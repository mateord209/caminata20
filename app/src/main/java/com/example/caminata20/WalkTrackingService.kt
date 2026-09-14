package com.example.caminata20

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalkTrackingService : Service(), SensorEventListener {

    companion object {
        const val ACTION_START = "com.example.caminata20.action.START_WALK"
        const val ACTION_STOP = "com.example.caminata20.action.STOP_WALK"
        const val CHANNEL_ID = "walk_tracking_channel"
        const val NOTIF_ID = 2001

        @Volatile var isTracking = false
        @Volatile var steps = 0
        @Volatile var km = 0.0
    }

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var initialSteps = -1

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var lastLocation: Location? = null

    private val handler = Handler(Looper.getMainLooper())
    private val timeoutRunnable = Runnable { stopTracking() }
    private val twoHoursMillis = 2 * 60 * 60 * 1000L

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000L).build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            if (location.hasAccuracy() && location.accuracy > 15f) return
            if (lastLocation != null) {
                val distance = lastLocation!!.distanceTo(location)
                if (distance > 5f) {
                    km += distance / 1000.0
                    lastLocation = location
                    updateNotification()
                }
            } else {
                lastLocation = location
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createChannel()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopTracking()
            return START_NOT_STICKY
        }

        if (!isTracking) {
            isTracking = true
            steps = 0
            km = 0.0
            initialSteps = -1
            lastLocation = null

            startForeground(NOTIF_ID, buildNotification())

            stepSensor?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST) }
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            handler.postDelayed(timeoutRunnable, twoHoursMillis)
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            if (initialSteps < 0) initialSteps = event.values[0].toInt()
            steps = event.values[0].toInt() - initialSteps
            updateNotification()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun stopTracking() {
        if (!isTracking) return
        isTracking = false
        handler.removeCallbacks(timeoutRunnable)
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)

        val savedSteps = steps
        val savedKm = km
        saveSession(savedSteps, savedKm)
        showFinishedNotification(savedSteps, savedKm)

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun saveSession(steps: Int, km: Double) {
        val prefs = getSharedPreferences("walk_stats", Context.MODE_PRIVATE)
        val totalSteps = prefs.getInt("total_steps", 0) + steps
        val totalKm = prefs.getFloat("total_km", 0f) + km.toFloat()
        val weekKm = prefs.getFloat("week_km", 0f) + km.toFloat()

        val sessionsJson = prefs.getString("sessions", "[]")
        val sessionsArray = JSONArray(sessionsJson)
        val newSession = JSONObject()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
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

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Seguimiento de caminata", NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Muestra el progreso de tu caminata en curso"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val openIntent = Intent(this, WalkActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, openIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Caminata en curso")
            .setContentText("$steps pasos · ${"%.2f".format(km)} km")
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID, buildNotification())
    }

    private fun showFinishedNotification(steps: Int, km: Double) {
        val mensaje = if (steps >= 1000 || km >= 0.8) {
            "¡Felicitaciones! Completaste $steps pasos (${"%.2f".format(km)} km) 🎉"
        } else {
            "Caminaste $steps pasos (${"%.2f".format(km)} km). ¡Intentá caminar un poco más!"
        }
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("Caminata guardada")
            .setContentText(mensaje)
            .setAutoCancel(true)
            .build()
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIF_ID + 1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(timeoutRunnable)
    }
}