package com.example.caminata20.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.domain.model.ConfigHidratacion
import com.example.caminata20.domain.model.EscalaTexto
import com.example.caminata20.domain.model.ReminderScheduleConfig
import com.example.caminata20.domain.model.TemaApp
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.DarkModeToggle
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.SelectableChip
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.showToast

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) {
        state.message?.let { context.showToast(it); vm.consumeMessage() }
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Ajustes", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text("Intervalo de pausas", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderScheduleConfig.INTERVALOS_PREDEFINIDOS.forEach { min ->
                        SelectableChip("$min min", selected = state.intervalMin == min, onClick = { vm.setInterval(min) })
                    }
                }
                Text(
                    "Mínimo ${ReminderScheduleConfig.MIN_INTERVAL} minutos entre alertas.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            Text("Días activos", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReminderScheduleConfig.ordenSemana().forEach { day ->
                        SelectableChip(
                            ReminderScheduleConfig.nombreDia(day),
                            selected = day in state.activeDays,
                            onClick = { vm.toggleDay(day) },
                        )
                    }
                }
            }

            Text("Modo reunión", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                Text(
                    if (state.meetingActive) "Las pausas están silenciadas temporalmente."
                    else "Silencia las alertas durante una reunión.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    SecondaryButton("30 min", onClick = { vm.enableMeeting(30) })
                    SecondaryButton("1 h", onClick = { vm.enableMeeting(60) })
                    SecondaryButton("Desactivar", onClick = vm::disableMeeting)
                }
            }

            Text("Hidratación", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Recordatorios de agua", modifier = Modifier.weight(1f))
                    Switch(checked = state.hidratacion.activo, onCheckedChange = vm::setHydration)
                }
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                    ConfigHidratacion.INTERVALOS.forEach { min ->
                        SelectableChip("Cada $min min", selected = state.hidratacion.intervaloMin == min, onClick = { vm.setHydrationInterval(min) })
                    }
                }
            }

            Text("Perfiles de horario", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                listOf(0 to state.profiles.perfil1, 1 to state.profiles.perfil2).forEach { (idx, perfil) ->
                    val activo = state.profiles.indiceActivo == idx
                    Text(
                        "${perfil.nombre}${if (activo) " · activo" else ""}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Text(
                        "${perfil.intervalMin} min · ${perfil.activeDays.size} días",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(vertical = 4.dp)) {
                        SecondaryButton("Guardar aquí", onClick = { vm.saveCurrentAsProfile(idx) }, modifier = Modifier.weight(1f))
                        PrimaryButton("Activar", onClick = { vm.activateProfile(idx) }, modifier = Modifier.weight(1f))
                    }
                }
            }

            Text("Apariencia", style = MaterialTheme.typography.titleLarge)
            BreakeeCard {
                Text(
                    "El tema y el tamaño del texto se aplican al guardar.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DarkModeToggle(
                    tema = state.apariencia.tema,
                    onToggle = vm::setDarkMode,
                    modifier = Modifier.padding(top = 8.dp),
                )
                SelectableChip(
                    label = "Seguir el tema del sistema",
                    selected = state.apariencia.tema == TemaApp.SISTEMA,
                    onClick = { vm.setTheme(TemaApp.SISTEMA) },
                    modifier = Modifier.padding(top = 4.dp),
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    EscalaTexto.entries.forEach { escala ->
                        SelectableChip(
                            when (escala) {
                                EscalaTexto.NORMAL -> "Normal (100 %)"
                                EscalaTexto.GRANDE -> "Grande (120 %)"
                                EscalaTexto.EXTRA_GRANDE -> "Extra grande (140 %)"
                            },
                            selected = state.apariencia.escalaTexto == escala,
                            onClick = { vm.setTextScale(escala) },
                        )
                    }
                }
            }

            PrimaryButton(
                text = if (state.saved) "Guardado ✓" else "Guardar ajustes",
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
