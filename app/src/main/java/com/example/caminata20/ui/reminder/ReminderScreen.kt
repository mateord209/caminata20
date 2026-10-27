package com.example.caminata20.ui.reminder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import com.example.caminata20.NotificationHelper
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.showToast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderScreen(onBack: () -> Unit, vm: ReminderViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val picker = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = false)
    LaunchedEffect(Unit) { NotificationHelper.createChannel(context) }
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    LaunchedEffect(state.done) { if (state.done) onBack() }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) vm.set(picker.hour, picker.minute) else context.showToast("Se necesita permiso de notificaciones")
    }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Recordatorio diario", onBack = onBack)
        Column(
            Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                state.active?.let { "Recordatorio activo a las ${it.label}" } ?: "Sin recordatorio activo",
                style = MaterialTheme.typography.titleMedium,
            )
            TimePicker(state = picker)
            PrimaryButton("Activar recordatorio", icon = Icons.Filled.AlarmOn, modifier = Modifier.fillMaxWidth(), onClick = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                    ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                ) notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                else vm.set(picker.hour, picker.minute)
            })
            SecondaryButton("Cancelar recordatorio", icon = Icons.Filled.AlarmOff, onClick = vm::cancel, modifier = Modifier.fillMaxWidth())
        }
    }
}
