package com.example.caminata20.ui.home

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.caminata20.MainActivity
import com.example.caminata20.ui.Routes
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.SecondaryButton
import com.example.caminata20.ui.components.TopBar

/** Pestaña Inicio. Enlaza con las pantallas XML existentes (caminata, IA, recordatorios). */
@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    val context = LocalContext.current
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Inicio")
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            HomeExtraSections(onNavigate)
            BreakeeCard {
                Text("Caminata y herramientas", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Seguimiento de caminatas, ejercicios con IA y recordatorio diario.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
                SecondaryButton(
                    text = "Abrir",
                    icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                    onClick = { context.startActivity(Intent(context, MainActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

/** Secciones que añaden las HU (tipo de pausa, perfil...). */
@Composable
private fun HomeExtraSections(onNavigate: (String) -> Unit) {
    // HU-08
    BreakeeCard(highlighted = true, onClick = { onNavigate(Routes.PAUSE_TYPE) }) {
        Text("Tipo de pausa", style = MaterialTheme.typography.titleLarge)
        Text("Estiramiento, descanso visual, respiración o hidratación", style = MaterialTheme.typography.bodyMedium)
    }
}
