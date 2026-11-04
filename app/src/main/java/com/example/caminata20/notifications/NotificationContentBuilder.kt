package com.example.caminata20.notifications

/** HU-07: contenido de una notificación de pausa, independiente de Android para poder probarlo. */
data class NotificationContent(
    val title: String,
    val text: String,
    val bigTitle: String,
    val bigText: String,
    val subText: String,
)

/** HU-07: arma el contenido con el ejercicio y la frase motivacional. */
fun buildPauseNotificationContent(
    typeEmoji: String,
    typeTitle: String,
    exerciseName: String,
    instructions: String,
    durationLabel: String,
    area: String,
    phrase: String,
): NotificationContent = NotificationContent(
    title = "$typeEmoji $typeTitle",
    text = "$exerciseName · $durationLabel",
    bigTitle = "$typeEmoji $typeTitle — $exerciseName",
    bigText = "$instructions\n($durationLabel · $area)\n\n💡 $phrase",
    subText = phrase,
)
