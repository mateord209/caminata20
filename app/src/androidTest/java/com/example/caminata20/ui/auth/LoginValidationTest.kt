package com.example.caminata20.ui.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.caminata20.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** HU-10: validación inline de los campos obligatorios del login. */
@RunWith(AndroidJUnit4::class)
class LoginValidationTest {

    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun muestraErroresInlineAlIntentarIngresarSinDatos() {
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Omitir").fetchSemanticsNodes().isNotEmpty() ||
                compose.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty()
        }
        val onboarding = compose.onAllNodesWithText("Omitir").fetchSemanticsNodes()
        if (onboarding.isNotEmpty()) {
            compose.onAllNodesWithText("Omitir")[0].performClick()
        }
        compose.waitUntil(15_000) {
            compose.onAllNodesWithText("Ingresar").fetchSemanticsNodes().isNotEmpty()
        }

        compose.onNodeWithText("Ingresar").performClick()

        compose.onNodeWithText("Ingresá tu correo").assertIsDisplayed()
        compose.onNodeWithText("Ingresá tu contraseña").assertIsDisplayed()
    }
}
