package com.example.caminata20.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/** Pestañas de la barra inferior. `route` coincide con las rutas de Navigation Compose. */
enum class BreakeeTab(val route: String, val label: String, val icon: ImageVector) {
    INICIO("inicio", "Inicio", Icons.Filled.Home),
    ALERTAS("alertas", "Alertas", Icons.Filled.Notifications),
    MI_DIA("mi_dia", "Mi Día", Icons.Filled.CalendarToday),
    ESTADISTICAS("estadisticas", "Estadísticas", Icons.Filled.BarChart),
}

@Composable
fun BottomNavBar(currentRoute: String?, onSelect: (BreakeeTab) -> Unit, modifier: Modifier = Modifier) {
    NavigationBar(modifier = modifier, containerColor = MaterialTheme.colorScheme.surface) {
        BreakeeTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = currentRoute == tab.route,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

/**
 * Encabezado con sobrelínea con la marca ("BREAKEE") + título grande, y botón atrás opcional.
 * HU-19: altura mínima de 64 dp que crece con el texto (sin recortes a 140 %).
 */
@Composable
fun TopBar(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = "BREAKEE",
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .heightIn(min = 64.dp)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
            }
        }
        Column(
            Modifier
                .weight(1f)
                .padding(start = if (onBack != null) 8.dp else 12.dp, end = 8.dp),
        ) {
            if (overline != null) {
                Text(overline, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(title, style = MaterialTheme.typography.headlineMedium)
        }
        actions()
    }
}
