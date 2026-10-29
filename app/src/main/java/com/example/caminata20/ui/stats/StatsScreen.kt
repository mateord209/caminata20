package com.example.caminata20.ui.stats

import android.content.Intent
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
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
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
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PauseTypeIcon
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.ProgressBar
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.theme.BreakeeThemeExt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** HU-04, HU-05, HU-09, HU-17, HU-18. */
@Composable
fun StatsScreen(title: String, vm: StatsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val locale = Locale.forLanguageTag("es-CO")
    val hourFmt = SimpleDateFormat("dd/MM HH:mm", locale)
    val status = BreakeeThemeExt.status

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

            PrimaryButton(
                "Compartir resumen",
                onClick = {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, state.shareText)
                    }
                    context.startActivity(Intent.createChooser(intent, "Compartir resumen semanal"))
                },
                icon = Icons.Filled.Share,
                modifier = Modifier.fillMaxWidth(),
            )

            Text("Historial de pausas completadas", style = MaterialTheme.typography.titleLarge)
            if (state.history.isEmpty()) {
                Text("Aún no hay pausas completadas registradas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            state.history.forEach { r ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    PauseTypeIcon(r.type, size = 28.dp)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(r.type.title, style = MaterialTheme.typography.bodyMedium)
                        Text(hourFmt.format(Date(r.timestamp)), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("✓", color = status.success)
                }
            }
        }
    }
}
