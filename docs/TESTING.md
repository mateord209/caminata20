# Pruebas

## Unitarias (JVM)

```bash
./gradlew testDebugUnitTest
```

No llaman a Firebase ni a un emulador. Cubren dominio, políticas, mapeos y ViewModels con fakes.
Resultado actual: 136 tests.

## Compose UI (instrumentadas)

```bash
./gradlew connectedDebugAndroidTest
```

Requiere un emulador o dispositivo. Cubren:

- `LoginValidationTest`: errores inline al intentar ingresar sin datos (HU-10).
- `StreakCardTest`: racha, progreso y estado “En riesgo” (HU-17).

## Maestro (E2E)

Instalación en Windows:

1. Descargar `maestro.zip` desde <https://github.com/mobile-dev-inc/Maestro/releases/latest>.
2. Extraer en `C:\maestro`.
3. Usar `C:\maestro\maestro\bin\maestro.bat` o agregar `C:\maestro\maestro\bin` al `PATH`.

Flujos:

```bash
maestro test .maestro/login-validation.yaml
maestro test -e TEST_EMAIL=... -e TEST_PASSWORD=... .maestro/streak.yaml
```

- `login-validation.yaml`: limpia el estado, omite el onboarding y verifica los errores inline.
- `streak.yaml`: inicia sesión con una cuenta de prueba y verifica la tarjeta “Racha actual”.

No commitees credenciales. En CI se leen de los secretos `TEST_EMAIL` y `TEST_PASSWORD`; si no
existen, el flujo de login real se omite y solo corre `login-validation`.

## CI

- `.github/workflows/android-release.yml`: tests unitarios + `assembleRelease`/`bundleRelease`.
- `.github/workflows/android-ui-tests.yml`: Compose UI en emulador API 34 + Maestro smoke.

## Evidencia manual del sprint

- Login con errores inline y Google Sign-In en emulador API 37.
- Notificación de pausa con frase motivacional (subText + cuerpo).
- Dashboard con “Racha actual”, “Hoy: 3/4” y “En riesgo”.
- Firestore: `users/{uid}/settings/goalHistory` con periodos `{from, goal}` y
  `users/{uid}/settings/meetingDays` con los días de Modo Reunión.
