package com.example.caminata20.ui.ai

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.formatDateTime
import com.example.caminata20.ui.util.showToast
import com.example.caminata20.ui.walk.ConfirmDeleteDialog

@Composable
fun AiExercisesScreen(onBack: () -> Unit, onFinished: () -> Unit, vm: AiExercisesViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    LaunchedEffect(state.finished) { if (state.finished) onFinished() }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Ejercicios con IA", onBack = onBack)
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            PrimaryButton("Generar rutina", icon = Icons.Filled.AutoAwesome, onClick = vm::generate, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
            if (state.loading) CircularProgressIndicator()
            state.current?.let { ex ->
                BreakeeCard(highlighted = true, modifier = Modifier.fillMaxWidth()) {
                    Text("Ejercicio ${state.index + 1} de ${state.exercises.size}", style = MaterialTheme.typography.labelMedium)
                    Text(AiExercisesViewModel.emojiForCategory(ex.category), fontSize = 56.sp)
                    Text(ex.name, style = MaterialTheme.typography.titleLarge)
                    Text("${ex.sets} series x ${ex.reps} repeticiones", style = MaterialTheme.typography.titleMedium)
                    Text(ex.instructions, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
                }
                PrimaryButton(
                    if (state.isLast) "Finalizar rutina" else "Completado, siguiente",
                    onClick = vm::next,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@Composable
fun AiHistoryScreen(onBack: () -> Unit, vm: AiHistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    if (state.confirmDelete) {
        ConfirmDeleteDialog("¿Estás seguro de eliminar ${state.selected.size} rutina(s)? Esta acción no se puede deshacer.", vm::confirmDelete, vm::dismissDelete)
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Historial de rutinas IA", onBack = onBack)
        if (state.routines.isEmpty()) {
            Text("Todavía no completaste ninguna rutina de estiramientos.", modifier = Modifier.padding(24.dp))
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.routines, key = { it.id }) { r ->
                BreakeeCard(onClick = { vm.toggle(r.id) }) {
                    Row(verticalAlignment = Alignment.Top) {
                        Checkbox(checked = r.id in state.selected, onCheckedChange = { vm.toggle(r.id) })
                        Column {
                            Text(formatDateTime(r.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            r.exercises.forEach {
                                Text("• ${it.name} — ${it.sets}x${it.reps}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
        if (state.routines.isNotEmpty()) {
            SecondaryButton("Eliminar seleccionadas", icon = Icons.Filled.Delete, onClick = vm::requestDelete, modifier = Modifier.fillMaxWidth().padding(24.dp))
        }
    }
}
