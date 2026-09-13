package com.example.caminata20

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Calendar

class ReminderActivity : AppCompatActivity() {

    private val prefsName = "reminder_prefs"
    private val requestNotifPermission = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        NotificationHelper.createChannel(this)

        val timePicker = findViewById<TimePicker>(R.id.timePicker)
        val tvCurrentReminder = findViewById<TextView>(R.id.tvCurrentReminder)
        val btnSetReminder = findViewById<Button>(R.id.btnSetReminder)
        val btnCancelReminder = findViewById<Button>(R.id.btnCancelReminder)

        timePicker.setIs24HourView(false)

        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedHour = prefs.getInt("hour", -1)
        val savedMinute = prefs.getInt("minute", -1)
        if (savedHour >= 0) {
            tvCurrentReminder.text = "Recordatorio activo a las %02d:%02d".format(savedHour, savedMinute)
        }

        btnSetReminder.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), requestNotifPermission
                )
                return@setOnClickListener
            }

            val hour = timePicker.hour
            val minute = timePicker.minute

            scheduleReminder(hour, minute)

            prefs.edit().putInt("hour", hour).putInt("minute", minute).apply()
            Toast.makeText(this, "Recordatorio activado", Toast.LENGTH_SHORT).show()
            finish()
        }

        btnCancelReminder.setOnClickListener {
            cancelReminder()
            prefs.edit().clear().apply()
            tvCurrentReminder.text = "Sin recordatorio activo"
            Toast.makeText(this, "Recordatorio cancelado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleReminder(hour: Int, minute: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun cancelReminder() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}