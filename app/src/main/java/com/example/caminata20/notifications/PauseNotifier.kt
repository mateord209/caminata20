package com.example.caminata20.notifications

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.caminata20.BreakeeActivity
import com.example.caminata20.data.ExerciseCatalog
import com.example.caminata20.ui.Routes
import kotlinx.coroutines.flow.first
import com.example.caminata20.NotificationHelper
import com.example.caminata20.data.SettingsRepository

/** Construye y muestra la notificación de pausa activa. */
object PauseNotifier {
    const val NOTIFICATION_ID = 1001

    suspend fun show(context: Context) {
        NotificationHelper.createChannel(context)
        val settings = SettingsRepository(context).current()
        val type = settings.type
        val repo = SettingsRepository(context)
        // HU-16: ejercicio del catálogo local (rota dentro del tipo elegido)
        val exercise = ExerciseCatalog.next(type, repo.lastExerciseId.first())
        repo.setLastExercise(exercise.id)

        // Al tocar la notificación se abre la pantalla guía del ejercicio
        val openGuideIntent = Intent(context, BreakeeActivity::class.java)
            .putExtra(BreakeeActivity.EXTRA_ROUTE, Routes.guide(exercise.id))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            context, 0, openGuideIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            // HU-08: la notificación muestra la categoría elegida
            .setContentTitle("${type.emoji} ${type.notificationTitle}")
            .setContentText("${exercise.name} · ${exercise.durationLabel}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle("${type.emoji} ${type.notificationTitle} — ${exercise.name}")
                    .bigText("${exercise.instructions}\n(${exercise.durationLabel} · ${exercise.area})")
            )
            .setSubText(type.title)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(longArrayOf(0, 500, 200, 500, 200, 500))
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            // HU-03: acciones directas
            .addAction(0, "✓ Completar", actionIntent(context, PauseActionReceiver.ACTION_COMPLETE, 11))
            .addAction(0, "⏱ Posponer 5 min", actionIntent(context, PauseActionReceiver.ACTION_SNOOZE, 12))

        notify(context, builder.build())
    }

    private fun actionIntent(context: Context, action: String, requestCode: Int): PendingIntent =
        PendingIntent.getBroadcast(
            context, requestCode,
            Intent(context, PauseActionReceiver::class.java).setAction(action),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

    fun notify(context: Context, notification: android.app.Notification, id: Int = NOTIFICATION_ID) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }
}
