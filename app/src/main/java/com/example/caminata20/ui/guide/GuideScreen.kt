package com.example.caminata20.ui.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.caminata20.ui.components.PauseTypeIcon
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.SelectableChip
import com.example.caminata20.ui.components.TopBar

/** HU-16: pantalla guía del ejercicio (se abre desde la notificación o desde Mi Día). */
@Composable
fun GuideScreen(exerciseId: String?, onBack: () -> Unit, vm: GuideViewModel = hiltViewModel()) {
    LaunchedEffect(exerciseId) { vm.load(exerciseId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val ex = state.exercise ?: return
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxSize()) {
        TopBar(title = ex.name, overline = "EJERCICIO SUGERIDO", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier.fillMaxWidth().height(180.dp).background(cs.secondaryContainer, RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center,
            ) { PauseTypeIcon(ex.type, size = 120.dp, container = cs.secondaryContainer) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SelectableChip(ex.durationLabel, selected = true, onClick = {})
                SelectableChip(ex.area, selected = false, onClick = {})
                SelectableChip(ex.level, selected = false, onClick = {})
            }
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.clock, style = MaterialTheme.typography.displayLarge, color = cs.primary)
                Text(
                    if (state.completed) "¡Pausa completada!" else "Paso ${state.currentStep + 1} de ${ex.steps.size}",
                    style = MaterialTheme.typography.bodyMedium, color = cs.onSurfaceVariant,
                )
            }
            ex.steps.forEachIndexed { i, step ->
                val done = i < state.currentStep
                val current = i == state.currentStep && !state.completed
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(32.dp).background(if (done || current) cs.primary else cs.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (done) Icon(Icons.Filled.Check, contentDescription = null, tint = cs.onPrimary, modifier = Modifier.size(18.dp))
                        else Text("${i + 1}", color = if (current) cs.onPrimary else cs.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(step, fontWeight = if (current) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.bodyLarge)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                SecondaryButton(
                    text = if (state.running) "Pausar" else "Reanudar",
                    icon = if (state.running) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    onClick = vm::toggle,
                    enabled = !state.completed,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    text = if (state.completed) "Hecho" else "Completar",
                    icon = Icons.Filled.Check,
                    onClick = { if (state.completed) onBack() else vm.complete() },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
