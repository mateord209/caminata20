package com.example.caminata20.ui.auth.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.caminata20.R

/**
 * Botón de Google Sign-In reutilizable.
 *
 * La G es decorativa: el texto del botón ya aporta la etiqueta accesible. El `testTag`
 * permite verificar el icono en Compose UI sin acoplar el test a un recurso concreto.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
    ) {
        Image(
            painter = painterResource(R.drawable.ic_google_g),
            contentDescription = null,
            modifier = Modifier
                .size(20.dp)
                .testTag(GOOGLE_LOGO_TAG),
        )
        Spacer(Modifier.width(8.dp))
        Text("Continuar con Google")
    }
}

const val GOOGLE_LOGO_TAG = "google_logo"
