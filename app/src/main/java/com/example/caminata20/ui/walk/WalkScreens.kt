package com.example.caminata20.ui.walk

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.WalkTrackingService
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.StatCard
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.formatDateTime
import com.example.caminata20.ui.util.showToast

private fun walkPermissions(): Array<String> = buildList {
    add(Manifest.permission.ACCESS_FINE_LOCATION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) add(Manifest.permission.ACTIVITY_RECOGNITION)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
}.toTypedArray()

@Composable
fun WalkScreen(onBack: () -> Unit, onFinished: () -> Unit, vm: WalkViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    fun startService(action: String) = ContextCompat.startForegroundService(
        context, Intent(context, WalkTrackingService::class.java).setAction(action),
    )
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { res ->
        if (res.values.all { it }) startService(WalkTrackingService.ACTION_START)
        else context.showToast("Se necesitan los permisos para contar pasos y distancia")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Caminata", onBack = onBack)
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard("${state.steps}", "Pasos", Icons.AutoMirrored.Filled.DirectionsWalk, Modifier.weight(1f))
                StatCard("%.2f km".format(state.km), "Distancia", Icons.Filled.Route, Modifier.weight(1f))
            }
            if (!state.isTracking) {
                PrimaryButton("Iniciar caminata", icon = Icons.AutoMirrored.Filled.DirectionsWalk, modifier = Modifier.fillMaxWidth(), onClick = {
                    val missing = walkPermissions().filter {
                        ContextCompat.checkSelfPermission(context, it) != android.content.pm.PackageManager.PERMISSION_GRANTED
                    }
                    if (missing.isEmpty()) startService(WalkTrackingService.ACTION_START)
                    else permissionLauncher.launch(missing.toTypedArray())
                })
            } else {
                SecondaryButton("Finalizar caminata", icon = Icons.Filled.Stop, modifier = Modifier.fillMaxWidth(), onClick = {
                    context.startService(Intent(context, WalkTrackingService::class.java).setAction(WalkTrackingService.ACTION_STOP))
                    context.showToast("Caminata finalizada y guardada")
                    onFinished()
                })
            }
            Text(
                "El seguimiento continúa en segundo plano (GPS en primer plano) hasta que lo finalices o pasen 2 horas.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun WalkHistoryScreen(onBack: () -> Unit, vm: WalkHistoryViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    if (state.confirmDelete) {
        ConfirmDeleteDialog("¿Estás seguro de eliminar ${state.selected.size} recorrido(s)? Esta acción no se puede deshacer.", vm::confirmDelete, vm::dismissDelete)
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Historial de caminatas", onBack = onBack)
        if (state.sessions.isEmpty()) {
            Text("Todavía no registraste ninguna caminata.", modifier = Modifier.padding(24.dp))
        }
        LazyColumn(Modifier.weight(1f).padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.sessions, key = { it.id }) { s ->
                BreakeeCard(onClick = { vm.toggle(s.id) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = s.id in state.selected, onCheckedChange = { vm.toggle(s.id) })
                        Column {
                            Text(formatDateTime(s.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${s.steps} pasos · %.2f km".format(s.km), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
        if (state.sessions.isNotEmpty()) {
            SecondaryButton("Eliminar seleccionados", icon = Icons.Filled.Delete, onClick = vm::requestDelete, modifier = Modifier.fillMaxWidth().padding(24.dp))
        }
    }
}

@Composable
fun ConfirmDeleteDialog(text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Confirmar eliminación") },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onConfirm, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) { Text("Eliminar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
    )
}
