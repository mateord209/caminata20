package com.example.caminata20.ui.pausetype

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.caminata20.data.PauseType
import com.example.caminata20.ui.components.OptionCard
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SelectableChip
import com.example.caminata20.ui.components.TopBar

fun PauseType.icon(): ImageVector = when (this) {
    PauseType.ESTIRAMIENTO -> Icons.Filled.SelfImprovement
    PauseType.DESCANSO_VISUAL -> Icons.Filled.Visibility
    PauseType.RESPIRACION -> Icons.Filled.Air
    PauseType.HIDRATACION -> Icons.Filled.WaterDrop
}

/** HU-08: selección del tipo de pausa predeterminado. */
@Composable
fun PauseTypeScreen(onBack: () -> Unit, vm: PauseTypeViewModel = viewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Configuración de pausa", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Selecciona tu tipo de pausa predeterminada",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PauseType.entries.forEach { type ->
                OptionCard(
                    title = type.notificationTitle,
                    subtitle = type.description,
                    icon = type.icon(),
                    selected = state.selected == type,
                    onClick = { vm.select(type) },
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Duración:", style = MaterialTheme.typography.bodyMedium)
                listOf(1, 2, 3).forEach { m ->
                    SelectableChip("$m min", selected = state.durationMin == m, onClick = { vm.setDuration(m) })
                }
            }
            PrimaryButton(
                text = if (state.saved) "Guardado ✓" else "Guardar configuración",
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}
