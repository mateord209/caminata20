# Cierre Sprint 2 — HU de Thomas Montoya

**Periodo:** 3–12 de noviembre de 2026
**Responsable:** Thomas Montoya Magón (Backend)
**Alcance:** HU-07, HU-10 y HU-17 (12 puntos)

## Estado

| HU | Puntos | Estado | Evidencia principal |
|---|---|---|---|
| HU-07 · Frases motivacionales en recordatorios | 2 | Completada | PR #8, notificación real con frase |
| HU-10 · Crear cuenta e iniciar sesión | 5 | Completada | PR #7, validación inline y Google Sign-In |
| HU-17 · Racha de días consecutivos | 5 | Completada | PR #9, Dashboard y Firestore |
| QA transversal | — | Completada | PR #10, Compose UI + Maestro |

## Qué se implementó

### HU-10
- `AuthValidator` puro: campos obligatorios, formato de correo, contraseña mínima y confirmación.
- Errores inline por campo; los errores de servidor se muestran como mensaje general.
- `AuthErrorMapper`: credenciales inválidas, usuario inexistente, red, demasiados intentos, email
  en uso, contraseña débil y cuenta deshabilitada.
- `RegisterUserUseCase`: crea `users/{uid}` con nombre y email sin pisar una meta existente.
- `SignOutUseCase`: cierra Firebase, cancela alarmas de pausa/hidratación/recordatorio y limpia
  el estado local del usuario.
- Logo de Google corregido: el PNG anterior era un botón completo de 180×40 y se mostraba a
  20×20; ahora es una G vectorial de 24×24 en un composable reutilizable.

### HU-07
- Catálogo de 22 frases motivacionales y ergonómicas.
- `PhraseSelectionPolicy`: no repite la última frase si se mostró hace menos de 24 h.
- `LocalPhraseRepository` con `PhraseStateStore` en DataStore y `Clock`/`Random` inyectados.
- `NotificationContentBuilder`: la frase aparece en el subtítulo y en el cuerpo de la notificación.
- El “Tip del día” del Dashboard reacciona al mismo repositorio.

### HU-17
- `DayWindowCalculator` con zona horaria inyectada; sin `+86_400_000L` fijo.
- `StreakDayEvaluator` con reglas explícitas: contado, neutro (día inactivo o reunión) y roto.
- `CalculateStreakUseCase`: el día en curso no reinicia la racha; solo un día activo pasado sin
  meta la rompe.
- `MeetingDayRepository`: días de Modo Reunión en `users/{uid}/settings/meetingDays` con caché
  local en DataStore.
- `GoalHistoryRepository` (Adenda C2): `users/{uid}/settings/goalHistory` con periodos
  `{from, goal}`; cambiar la meta hoy no reevalúa los días cerrados.
- `ObserveStreakUseCase` alimenta el Dashboard con racha, progreso de hoy y estado “En riesgo”.
- `StreakCard` en Inicio.

## Pruebas

| Capa | Resultado |
|---|---|
| Unitarias JVM | **136 tests, 0 fallos** |
| Compose UI Test | **3 tests, 0 fallos** (emulador API 37) |
| Maestro | **2 flujos OK** (`login-validation`, `streak`) |
| `assembleDebug` | OK |
| CI | `Android release` + `Android UI tests` |

Flujos Maestro:
- `.maestro/login-validation.yaml`: limpia estado, omite onboarding y verifica errores inline.
- `.maestro/streak.yaml`: login con cuenta de prueba y verificación de “Racha actual”.

## Evidencia visual

- `docs/evidencia/login-google-vector.png`: login con la G vectorial.
- `docs/evidencia/notificacion-frase.png`: notificación con la frase en el subtítulo y el cuerpo.
- `docs/evidencia/dashboard-racha-c2.png`: Dashboard con “Racha actual 1 día”, “Hoy: 3/4” y
  “En riesgo” después de cambiar la meta (C2).
- Firestore `settings/goalHistory` con `{from: 0, goal: 3}` y `{from: hoy, goal: 4}`.

## Git

```
main
 └── develop
      ├── fix/google-logo      (#6)
      ├── feature/hu-10-auth   (#7)
      ├── feature/hu-07-frases (#8)
      ├── feature/hu-17-racha  (#9)
      └── test/qa-e2e          (#10)
```

## Deuda conocida

- `pauseHistory` limita a 500 registros; con meta 8/día la racha máxima calculable es ~62 días.
- `LocalStateRepository` sigue siendo un god object; no se amplió con frases ni días de reunión.
- `PauseNotifier` usa `ExerciseCatalog` directamente; se resolverá con un `ExerciseRepository`.
- `PauseUseCases.kt` y `SuggestedExercise.kt` importan `data.local.ExerciseCatalog` (deuda previa).
- Google Sign-In completo no se automatiza por 2FA; se prueba el botón, el intent y el repositorio
  con token falso.

## Guion de demo (3 minutos)

1. Abrir la app y mostrar el login: correo inválido → error inline; botón de Google con la G.
2. Iniciar sesión con la cuenta de prueba y mostrar el Dashboard: racha, “Hoy: 3/4”, “En riesgo”
   y “Tip del día”.
3. Ir a Ajustes → Modo Reunión y explicar que el día queda neutro para la racha.
4. Mostrar la notificación de pausa con la frase motivacional y las acciones Completar/Posponer.
5. Cerrar con la arquitectura: dominio puro, casos de uso inyectables, tests y CI.

## Documentos de Drive a actualizar

- `Estado por corte y trazabilidad HU–producto`: mover HU-07, HU-10 y HU-17 a “Completada” con
  enlace a los PR.
- `Checklist_Entregables_Electiva_V_2P2026 (diligenciado)`: marcar la evidencia de producto
  funcional y trazabilidad.
- `Breakee – Incremento del producto`: agregar los PR #7–#10 y el resultado de las pruebas.
