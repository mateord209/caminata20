package com.example.caminata20.ui.stats

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PauseHistoryRow
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.ProgressBar
import com.example.caminata20.ui.components.SelectableChip
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.components.icon

/** HU-04, HU-05, HU-09, HU-17, HU-18. */
@Composable
fun StatsScreen(title: String, vm: StatsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(Modifier.fillMaxSize()) {
        TopBar(title = title)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("${state.streak}", "Racha diaria", Icons.Filled.EmojiEvents, Modifier.weight(1f))
                StatCard("${state.weekly.lastOrNull()?.porcentaje ?: 0}%", "Hoy", Icons.Filled.TrendingUp, Modifier.weight(1f))
            }

            Text("Cumplimiento semanal", style = MaterialTheme.typography.titleLarge)
            state.weekly.forEach { dia ->
                BreakeeCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(dia.etiqueta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.width(72.dp))
                        Column(Modifier.weight(1f)) {
                            ProgressBar(progress = dia.porcentaje / 100f)
                            Text(
                                "${dia.completadas}/${dia.esperadas} pausas · ${dia.porcentaje}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            BreakeeCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Resumen semanal", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${state.weeklyCompleted} de ${state.weeklyExpected} pausas completadas",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        "${state.weeklyPercent}%",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            PrimaryButton(
                "Compartir resumen",
                onClick = {
                    val send = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, state.shareText)
                        putExtra(Intent.EXTRA_SUBJECT, "Mi resumen semanal · Breakee")
                    }
                    runCatching {
                        context.startActivity(Intent.createChooser(send, "Compartir resumen semanal"))
                    }
                },
                icon = Icons.Filled.Share,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Historial de pausas completadas", style = MaterialTheme.typography.titleLarge)
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
            if (state.history.isEmpty()) {
                Text(
                    text = if (state.typeFilter == null) {
                        "Aún no hay pausas completadas registradas."
                    } else {
                        "No hay pausas de esta categoría."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                state.history.forEach { record ->
                    PauseHistoryRow(record = record, showDate = true)
                }
            }
        }
    }
}
