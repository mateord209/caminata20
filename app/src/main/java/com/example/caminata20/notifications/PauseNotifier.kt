package com.example.caminata20.notifications

import android.Manifest
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.caminata20.BreakeeActivity
import com.example.caminata20.NotificationHelper
import com.example.caminata20.data.local.ExerciseCatalog
import com.example.caminata20.domain.repository.LocalStateRepository
import com.example.caminata20.domain.repository.PauseNotificationGateway
import com.example.caminata20.domain.repository.PreferencesRepository
import com.example.caminata20.domain.usecase.PauseActionsUseCase
import com.example.caminata20.ui.Routes
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/** Construye y muestra las notificaciones de pausa activa (HU-08, HU-16, HU-03). */
@Singleton
class PauseNotifier @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: PreferencesRepository,
    private val local: LocalStateRepository,
) : PauseNotificationGateway {

    suspend fun show() {
        NotificationHelper.createChannel(context)
        val settings = prefs.current()
        val type = settings.type
        // HU-16: ejercicio del catálogo local (rota dentro del tipo elegido)
        val exercise = ExerciseCatalog.next(type, local.lastExerciseId.first())
        local.setLastExercise(exercise.id)

        val openGuideIntent = Intent(context, BreakeeActivity::class.java)
            .putExtra(BreakeeActivity.EXTRA_ROUTE, Routes.guide(exercise.id))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val contentIntent = PendingIntent.getActivity(
            context, 0, openGuideIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val n = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
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
            .addAction(0, "✓ Completar", actionIntent(PauseActionReceiver.ACTION_COMPLETE, 11))
            .addAction(0, "⏱ Posponer 5 min", actionIntent(PauseActionReceiver.ACTION_SNOOZE, 12))
            .build()
        notify(n, NOTIFICATION_ID)
    }

    override fun dismissPause() {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        NotificationManagerCompat.from(context).cancel(WARNING_ID)
    }

    override fun showStreakWarning(consecutive: Int) {
        NotificationHelper.createChannel(context)
        val text = PauseActionsUseCase.streakWarningText(consecutive)
        val n = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("⚠️ Tu racha está en riesgo")
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        notify(n, WARNING_ID)
    }

    private fun actionIntent(action: String, requestCode: Int): PendingIntent = PendingIntent.getBroadcast(
        context, requestCode, Intent(context, PauseActionReceiver::class.java).setAction(action),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    private fun notify(notification: Notification, id: Int) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            NotificationManagerCompat.from(context).notify(id, notification)
        }
    }

    companion object {
        const val NOTIFICATION_ID = 1001
        const val WARNING_ID = 1002
    }
}
