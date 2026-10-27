package com.example.caminata20.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.caminata20.domain.model.PauseType

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
