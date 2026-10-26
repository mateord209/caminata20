package com.example.caminata20.ui.stats

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.TopBar

/** Placeholder. TODO(HU-04): estadísticas e historial completo (otro integrante del equipo). */
@Composable
fun StatsScreen(title: String) {
    Column(Modifier.fillMaxSize()) {
        TopBar(title = title)
        BreakeeCard(Modifier.padding(24.dp)) {
            Text("Próximamente", style = MaterialTheme.typography.titleLarge)
            Text("Esta sección la implementa otra HU del equipo.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
