package com.example.caminata20.ui.myday

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PauseHistoryRow
import com.example.caminata20.ui.components.PauseTypeIcon
import com.example.caminata20.ui.components.ProgressBar
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SelectableChip
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.components.icon
import com.example.caminata20.ui.theme.LocalTextScale
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** HU-04, HU-09, HU-12: Mi Día con progreso, racha e historial día/semana. */
@Composable
fun MyDayScreen(onOpenProfile: () -> Unit, onStartExercise: (String) -> Unit = {}, vm: MyDayViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    // HU-19: a 140 % el contador reduce su estilo para no comprimir la columna de progreso.
    val counterStyle = if (LocalTextScale.current >= 1.4f) MaterialTheme.typography.displaySmall
    else MaterialTheme.typography.displayLarge
    val locale = Locale.forLanguageTag("es-CO")
    val today = SimpleDateFormat("EEEE, d 'de' MMMM", locale).format(Date()).replaceFirstChar { it.titlecase(locale) }
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
                    Text("${state.completed}", style = counterStyle)
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
                StatCard("${state.streak}", "Racha", Icons.Filled.EmojiEvents, Modifier.weight(1f))
            }
            state.suggested?.let { ex ->
                Text("Ejercicio sugerido", style = MaterialTheme.typography.titleLarge)
                BreakeeCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        PauseTypeIcon(ex.type)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(ex.name, style = MaterialTheme.typography.titleMedium)
                            Text("${ex.durationLabel} · ${ex.area}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        PrimaryButton("Iniciar", onClick = { onStartExercise(ex.id) })
                    }
                }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Historial", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                SelectableChip(
                    label = "Día",
                    selected = state.range == HistoryRange.DIA,
                    onClick = { vm.setRange(HistoryRange.DIA) },
                )
                Spacer(Modifier.width(8.dp))
                SelectableChip(
                    label = "Semana",
                    selected = state.range == HistoryRange.SEMANA,
                    onClick = { vm.setRange(HistoryRange.SEMANA) },
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    SelectableChip(
                        label = "Todas",
                        selected = state.typeFilter == null,
                        onClick = { vm.onFilterType(null) },
                    )
                }
                items(PauseType.entries) { type ->
                    SelectableChip(
                        label = type.title,
                        selected = state.typeFilter == type,
                        onClick = { vm.onFilterType(type) },
                        leadingIcon = type.icon,
                    )
                }
            }
            if (state.visibleRecords.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        Icons.Filled.SelfImprovement,
                        contentDescription = null,
                        modifier = Modifier.size(96.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                    )
                    Text(
                        text = when {
                            state.typeFilter != null -> "No hay pausas de esta categoría."
                            state.range == HistoryRange.SEMANA -> "Aún no has registrado pausas en los últimos 7 días."
                            else -> "Aún no has registrado pausas el día de hoy. ¡Empieza tu primera pausa!"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            } else {
                state.visibleRecords.forEach { record ->
                    PauseHistoryRow(record = record, showDate = state.range == HistoryRange.SEMANA)
                }
            }
        }
    }
}
