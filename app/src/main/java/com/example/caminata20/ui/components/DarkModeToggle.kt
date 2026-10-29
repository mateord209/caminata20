package com.example.caminata20.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.caminata20.domain.model.TemaApp

/**
 * HU-13: interruptor para activar/desactivar el modo oscuro.
 * Se usa tanto en Ajustes como en Mi Perfil.
 *
 * Si el tema activo es [TemaApp.SISTEMA], el interruptor refleja el estado efectivo del
 * sistema; al tocarlo se fija un tema explícito (claro u oscuro).
 */
@Composable
fun DarkModeToggle(
    tema: TemaApp,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val systemDark = isSystemInDarkTheme()
    val darkEnabled = when (tema) {
        TemaApp.OSCURO -> true
        TemaApp.CLARO -> false
        TemaApp.SISTEMA -> systemDark
    }

    Row(
        modifier
            .fillMaxWidth()
            .toggleable(
                value = darkEnabled,
                role = Role.Switch,
                onValueChange = onToggle,
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text("Modo oscuro", style = MaterialTheme.typography.titleMedium)
            Text(
                when {
                    tema == TemaApp.SISTEMA -> "Siguiendo el tema del sistema"
                    darkEnabled -> "Activado"
                    else -> "Desactivado"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Switch(checked = darkEnabled, onCheckedChange = null)
    }
}
