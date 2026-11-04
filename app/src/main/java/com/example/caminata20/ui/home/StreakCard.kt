package com.example.caminata20.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.caminata20.domain.model.StreakResult
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.ProgressBar

/** Tarjeta stateless de racha para el Dashboard (HU-17). */
@Composable
fun StreakCard(streak: StreakResult, modifier: Modifier = Modifier) {
    BreakeeCard(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.EmojiEvents,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 16.dp),
            )
            Column(Modifier.weight(1f)) {
                Text("Racha actual", style = MaterialTheme.typography.labelMedium)
                Text("${streak.current} días", style = MaterialTheme.typography.titleLarge)
            }
            if (streak.atRisk) {
                Text(
                    "En riesgo",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
        if (streak.todayGoal > 0) {
            Text(
                "Hoy: ${streak.todayCompleted}/${streak.todayGoal}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp),
            )
            ProgressBar(
                progress = streak.todayCompleted.toFloat() / streak.todayGoal,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}
