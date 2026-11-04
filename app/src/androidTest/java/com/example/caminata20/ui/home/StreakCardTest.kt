package com.example.caminata20.ui.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.caminata20.domain.model.StreakResult
import com.example.caminata20.ui.theme.BreakeeTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4

/** HU-17: la tarjeta de racha muestra el estado real del Dashboard. */
@RunWith(AndroidJUnit4::class)
class StreakCardTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun muestraRachaProgresoYEstadoEnRiesgo() {
        compose.setContent {
            BreakeeTheme {
                StreakCard(
                    StreakResult(
                        current = 1,
                        todayCompleted = 3,
                        todayGoal = 4,
                        atRisk = true,
                    ),
                )
            }
        }

        compose.onNodeWithText("Racha actual").assertIsDisplayed()
        compose.onNodeWithText("1 día").assertIsDisplayed()
        compose.onNodeWithText("Hoy: 3/4").assertIsDisplayed()
        compose.onNodeWithText("En riesgo").assertIsDisplayed()
    }

    @Test
    fun diaInactivoNoMuestraProgresoDeHoy() {
        compose.setContent {
            BreakeeTheme {
                StreakCard(StreakResult(current = 2, todayCompleted = 0, todayGoal = 0))
            }
        }

        compose.onNodeWithText("2 días").assertIsDisplayed()
        compose.onNodeWithText("Hoy: 0/0").assertDoesNotExist()
    }
}
