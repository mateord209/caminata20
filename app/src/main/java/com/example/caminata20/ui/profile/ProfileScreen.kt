package com.example.caminata20.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import com.example.caminata20.ui.auth.googleSignInClient
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.caminata20.domain.model.Profile
import com.example.caminata20.ui.components.BreakeeCard
import com.example.caminata20.ui.components.DarkModeToggle
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.Stepper
import com.example.caminata20.ui.components.TopBar

/** HU-12: Mi Perfil. */
@Composable
fun ProfileScreen(onBack: () -> Unit, onSignedOut: () -> Unit, vm: ProfileViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val range = Profile.GOAL_RANGE
    LaunchedEffect(state.signedOut) { if (state.signedOut) onSignedOut() }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Mi Perfil", onBack = onBack)
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BreakeeCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(64.dp).background(MaterialTheme.colorScheme.secondaryContainer, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text(state.initials, style = MaterialTheme.typography.titleLarge) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(state.name.ifBlank { "Sin nombre" }, style = MaterialTheme.typography.titleLarge)
                        Text(state.email, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.size(16.dp))
                OutlinedTextField(
                    value = state.name,
                    onValueChange = vm::onNameChange,
                    label = { Text("Nombre") },
                    isError = state.nameError != null,
                    supportingText = state.nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    label = { Text("Correo (solo lectura)") },
                    trailingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            BreakeeCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Meta diaria", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Elige entre ${range.first} y ${range.last} pausas al día",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text("${state.goal} pausas", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                }
                Slider(
                    value = state.goal.toFloat(),
                    onValueChange = { vm.onGoalChange(it.toInt()) },
                    valueRange = range.first.toFloat()..range.last.toFloat(),
                    steps = range.last - range.first - 1,
                )
                Stepper(value = state.goal, onValueChange = vm::onGoalChange, range = range)
            }
            BreakeeCard {
                Text("Apariencia", style = MaterialTheme.typography.titleLarge)
                Text(
                    "Se aplica al pulsar Guardar cambios.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                DarkModeToggle(
                    tema = state.apariencia.tema,
                    onToggle = vm::setDarkMode,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            PrimaryButton(
                text = if (state.saved) "Guardado ✓" else "Guardar cambios",
                onClick = vm::save,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(onClick = {
                googleSignInClient(context).signOut()
                vm.signOut()
}, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }
}
