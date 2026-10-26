package com.example.caminata20.ui.myday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caminata20.data.PauseStatus
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.ProgressBar
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.StatusBadge
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.theme.BreakeeThemeExt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** HU-12: Mi Día. */
@Composable
fun MyDayScreen(onOpenProfile: () -> Unit, onStartExercise: (String) -> Unit = {}, vm: MyDayViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val locale = Locale.forLanguageTag("es-CO")
    val today = SimpleDateFormat("EEEE, d 'de' MMMM", locale).format(Date()).replaceFirstChar { it.titlecase(locale) }
    val hourFmt = SimpleDateFormat("HH:mm", locale)
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Mi Día", actions = {
            IconButton(onClick = onOpenProfile) { Icon(Icons.Filled.Person, contentDescription = "Mi Perfil") }
        })
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BreakeeCard(highlighted = true) {
                Text(today, style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${state.completed}", style = MaterialTheme.typography.displayLarge)
                    Text("/${state.goal}", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("pausas activas", style = MaterialTheme.typography.titleMedium)
                        ProgressBar(progress = state.progress, modifier = Modifier.padding(vertical = 8.dp))
                        Text("${state.percent}% de la meta diaria", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("${state.totalMinutes} min", "Tiempo total", Icons.Filled.Timer, Modifier.weight(1f))
                StatCard("${state.percent}%", "Meta de hoy", Icons.Filled.EmojiEvents, Modifier.weight(1f))
            }
            state.suggested?.let { ex ->
                Text("Ejercicio sugerido", style = MaterialTheme.typography.titleLarge)
                BreakeeCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(ex.type.emoji, style = MaterialTheme.typography.headlineMedium)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, style = MaterialTheme.typography.titleMedium)
                            Text("${ex.durationLabel} · ${ex.area}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PrimaryButton("Iniciar", onClick = { onStartExercise(ex.id) })
                    }
                }
            }
            Text("Historial de hoy", style = MaterialTheme.typography.titleLarge)
            if (state.todayRecords.isEmpty()) {
                Text("Aún no registras pausas hoy.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val status = BreakeeThemeExt.status
            state.todayRecords.forEach { r ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text("${hourFmt.format(Date(r.timestamp))} · ${r.type.title}", modifier = Modifier.weight(1f))
                    if (r.status == PauseStatus.COMPLETADA) {
                        StatusBadge("✓ Completada", MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer)
                    } else {
                        StatusBadge("Pospuesta", status.warningContainer, status.warning)
                    }
                }
            }
        }
    }
}
