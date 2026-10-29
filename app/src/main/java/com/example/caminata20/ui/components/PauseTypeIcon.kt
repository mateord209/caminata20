package com.example.caminata20.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.caminata20.domain.model.PauseRecord
import com.example.caminata20.domain.model.PauseStatus
import com.example.caminata20.domain.model.PauseType
import com.example.caminata20.ui.theme.BreakeeThemeExt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Mapeo ÚNICO tipo de pausa → ícono de Material Icons (HU-08). Úsalo en toda la UI. */
val PauseType.icon: ImageVector
    get() = when (this) {
        PauseType.ESTIRAMIENTO -> Icons.Filled.SelfImprovement
        PauseType.DESCANSO_VISUAL -> Icons.Filled.Visibility
        PauseType.RESPIRACION -> Icons.Filled.Air
        PauseType.HIDRATACION -> Icons.Filled.WaterDrop
    }

/**
 * Ícono del tipo de pausa dentro de un contenedor redondeado. Recibe el tipo como parámetro
 * (estado observado), así que se recompone en cuanto cambia la preferencia.
 */
@Composable
fun PauseTypeIcon(
    type: PauseType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    container: Color = MaterialTheme.colorScheme.primaryContainer,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(modifier.size(size).background(container, RoundedCornerShape(size / 3)), contentAlignment = Alignment.Center) {
        Crossfade(targetState = type, label = "pauseTypeIcon") { t ->
            Icon(t.icon, contentDescription = t.title, tint = tint, modifier = Modifier.size(size * 0.6f))
        }
    }
}

/** Ícono + etiqueta del tipo de pausa (p. ej. "Hidratación"). */
@Composable
fun PauseTypeLabel(
    type: PauseType,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleSmall,
    iconSize: Dp = 28.dp,
) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        PauseTypeIcon(type, size = iconSize)
        Spacer(Modifier.width(8.dp))
        Text(type.title, style = style)
    }
}

/** HU-09: etiqueta visual de categoría con su ícono asociativo (CA-01). */
@Composable
fun PauseTypeBadge(type: PauseType, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    Surface(
        color = colors.primaryContainer,
        contentColor = colors.onPrimaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier,
    ) {
        Row(
            Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Icon(type.icon, contentDescription = type.title, tint = colors.primary, modifier = Modifier.size(16.dp))
            Text(type.title, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/**
 * HU-09: fila reutilizable del historial de pausas (Mi Día y Estadísticas).
 * `showDate` añade la fecha `dd/MM` delante de la hora (vista semanal).
 */
@Composable
fun PauseHistoryRow(
    record: PauseRecord,
    showDate: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val locale = Locale.forLanguageTag("es-CO")
    val timeFmt = remember { SimpleDateFormat("HH:mm", locale) }
    val dateFmt = remember { SimpleDateFormat("dd/MM", locale) }
    val status = BreakeeThemeExt.status
    Row(modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PauseTypeBadge(record.type)
            val hora = timeFmt.format(Date(record.timestamp))
            Text(
                text = if (showDate) "${dateFmt.format(Date(record.timestamp))} · $hora" else hora,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.width(8.dp))
        if (record.status == PauseStatus.COMPLETADA) {
            StatusBadge(
                "✓ Completada",
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.onPrimaryContainer,
            )
        } else {
            StatusBadge("Pospuesta", status.warningContainer, status.warning)
        }
    }
}
