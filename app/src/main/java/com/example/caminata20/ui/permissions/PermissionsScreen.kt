package com.example.caminata20.ui.permissions

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.showToast

/** Solicita permisos críticos para recordatorios (HU-02). */
@Composable
fun PermissionsScreen(onBack: () -> Unit, onDone: () -> Unit) {
    val context = LocalContext.current
    var notifOk by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED,
        )
    }
    val notifLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        notifOk = granted
        if (!granted) context.showToast("Sin notificaciones no recibirás las pausas.")
    }

    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Permisos", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BreakeeCard {
                Text("Notificaciones", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Breakee avisa con vibración y mensaje cuando toca tu pausa activa.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                PrimaryButton(
                    if (notifOk) "Notificaciones concedidas ✓" else "Permitir notificaciones",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notifOk) {
                            notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    },
                    enabled = !notifOk,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BreakeeCard {
                Text("Alarmas exactas", style = MaterialTheme.typography.titleMedium)
                Text(
                    "En Android 12+ puede ser necesario autorizar alarmas exactas para intervalos precisos.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SecondaryButton(
                    "Abrir ajustes del sistema",
                    onClick = {
                        val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                            data = android.net.Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            PrimaryButton("Continuar", onClick = onDone, modifier = Modifier.fillMaxWidth())
        }
    }
}
