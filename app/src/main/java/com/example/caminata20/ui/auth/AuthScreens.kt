package com.example.caminata20.ui.auth

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.caminata20.R
import com.example.caminata20.ui.components.PrimaryButton
import com.example.caminata20.ui.components.TopBar
import com.example.caminata20.ui.util.showToast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

/** Cliente de Google Sign-In (mismo flujo que la LoginActivity XML anterior). */
fun googleSignInClient(context: Context): GoogleSignInClient = GoogleSignIn.getClient(
    context,
    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(context.getString(R.string.default_web_client_id))
        .requestEmail()
        .build(),
)

@Composable
private fun AuthField(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboard: KeyboardType = KeyboardType.Text,
    password: Boolean = false,
) {
    var visible by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        trailingIcon = if (password) {
            {
                IconButton(onClick = { visible = !visible }) {
                    Icon(
                        if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña",
                    )
                }
            }
        } else null,
        visualTransformation = if (password && !visible) PasswordVisualTransformation('*') else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = if (password) KeyboardType.Password else keyboard),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun AuthColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) { content() }
}

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    onRegister: () -> Unit,
    onForgot: () -> Unit,
    vm: LoginViewModel = hiltViewModel(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val client = remember { googleSignInClient(context) }
    val googleLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        try {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data).getResult(ApiException::class.java)
            vm.signInWithGoogle(account.idToken)
        } catch (e: ApiException) {
            vm.onGoogleError(e.statusCode)
        }
    }
    LaunchedEffect(state.loggedIn) { if (state.loggedIn) onLoggedIn() }
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }

    AuthColumn {
        Spacer(Modifier.height(32.dp))
        Image(painterResource(R.drawable.logo_breakee), contentDescription = null, modifier = Modifier.size(110.dp))
        Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)
        AuthField(state.email, vm::onEmail, "Correo", Icons.Filled.Email, KeyboardType.Email)
        AuthField(state.password, vm::onPassword, "Contraseña", Icons.Filled.Lock, password = true)
        TextButton(onClick = onForgot, modifier = Modifier.align(Alignment.End)) { Text("¿Olvidaste tu contraseña?") }
        if (state.loading) CircularProgressIndicator()
        PrimaryButton("Ingresar", onClick = vm::signIn, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
        OutlinedButton(
            onClick = { googleLauncher.launch(client.signInIntent) },
            enabled = !state.loading,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        ) {
            Image(painterResource(R.drawable.ic_google), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continuar con Google")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("¿No tenés cuenta?")
            TextButton(onClick = onRegister) { Text("Registrate") }
        }
    }
}

@Composable
fun RegisterScreen(onRegistered: () -> Unit, onBack: () -> Unit, vm: RegisterViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    LaunchedEffect(state.registered) { if (state.registered) onRegistered() }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Crear cuenta", onBack = onBack)
        AuthColumn {
            AuthField(state.name, vm::onName, "Nombre", Icons.Filled.Person)
            AuthField(state.email, vm::onEmail, "Correo", Icons.Filled.Email, KeyboardType.Email)
            AuthField(state.password, vm::onPassword, "Contraseña", Icons.Filled.Lock, password = true)
            AuthField(state.confirm, vm::onConfirm, "Confirmar contraseña", Icons.Filled.Lock, password = true)
            if (state.loading) CircularProgressIndicator()
            PrimaryButton("Registrarme", onClick = vm::register, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
            TextButton(onClick = onBack) { Text("¿Ya tenés cuenta? Iniciá sesión") }
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit, vm: ForgotPasswordViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(state.message) { state.message?.let { context.showToast(it); vm.consumeMessage() } }
    LaunchedEffect(state.sent) { if (state.sent) onBack() }
    Column(Modifier.fillMaxSize()) {
        TopBar(title = "Recuperar contraseña", onBack = onBack)
        AuthColumn {
            Text(
                "Ingresá tu correo y te enviaremos un enlace para restablecer la contraseña.",
                style = MaterialTheme.typography.bodyMedium,
            )
            AuthField(state.email, vm::onEmail, "Correo", Icons.Filled.Email, KeyboardType.Email)
            if (state.loading) CircularProgressIndicator()
            PrimaryButton("Enviar enlace", onClick = vm::send, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
        }
    }
}
