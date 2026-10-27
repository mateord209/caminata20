# Breakee (caminata20)

App Android de pausas activas en Kotlin + Jetpack Compose (una sola Activity, MVVM, Hilt,
Firebase Auth y Cloud Firestore). `applicationId`/paquete: `com.example.caminata20`.

## Compilar en local

Requisitos: JDK 17 y Android SDK (compileSdk 36).

```bash
./gradlew testDebugUnitTest   # tests unitarios
./gradlew assembleDebug       # APK debug
./gradlew assembleRelease     # APK release (firmada con la clave debug si no hay keystore)
./gradlew bundleRelease       # AAB release
```

`local.properties` (no se commitea) puede llevar `GROQ_API_KEY`, `GROQ_PROXY_URL` y la firma
(`KEYSTORE_FILE`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`). Ver `docs/SEGURIDAD_GROQ.md`
sobre el riesgo de embeber `GROQ_API_KEY` en el APK.

## CI: build y publicación de la release

Workflow: [`.github/workflows/android-release.yml`](.github/workflows/android-release.yml).

| Evento | Qué hace |
|---|---|
| PR hacia `main` | tests + `assembleRelease` + `bundleRelease`, sube el APK/AAB como artifact |
| push a `developRamirez` | lo anterior + **prerelease rodante `latest-dev`** (se reemplaza en cada push) |
| push a `main` | lo anterior + release rodante **`latest`** |
| tag `v*` (p. ej. `v1.2.0`) | lo anterior + **GitHub Release `v1.2.0`** con notas generadas |
| `workflow_dispatch` | build manual (solo artifact) |

Pasos: JDK 17 (Temurin), caché de Gradle (`gradle/actions/setup-gradle`), `testDebugUnitTest`,
`assembleRelease`, `bundleRelease`, artifact y publicación con `softprops/action-gh-release`
(`permissions: contents: write`).

Versionado: `versionCode` = número de ejecución del workflow; `versionName` = el tag sin la `v`
(o `1.0.<n>` fuera de tags).

Nombres de los archivos:

- `breakee-<versión>-release-signed.apk|aab` → firmada con tu keystore (los 4 secrets existen).
- `breakee-<versión>-release-debugsigned.apk|aab` → **sin secrets de firma**: firmada con la clave
  debug del runner. Instalable para pruebas, pero **no apta para Play Store**, y como la clave debug
  cambia en cada runner, no se puede actualizar encima de otra build: hay que desinstalar antes.

### Secrets (Settings → Secrets and variables → Actions)

| Secret | Obligatorio | Contenido |
|---|---|---|
| `KEYSTORE_BASE64` | para firmar | el `.jks` codificado en base64 |
| `KEYSTORE_PASSWORD` | para firmar | contraseña del keystore |
| `KEY_ALIAS` | para firmar | alias de la clave (p. ej. `breakee`) |
| `KEY_PASSWORD` | para firmar | contraseña de la clave |
| `GROQ_API_KEY` | no | se escribe en `local.properties` dentro del CI |
| `GROQ_PROXY_URL` | no | URL del proxy de Groq (recomendado en lugar de la key) |

### Crear el keystore y pasarlo a base64

```bash
keytool -genkeypair -v \
  -keystore breakee-release.jks -alias breakee \
  -keyalg RSA -keysize 2048 -validity 10000

# Linux
base64 -w 0 breakee-release.jks > breakee-release.jks.b64
# macOS
base64 -i breakee-release.jks | tr -d '\n' > breakee-release.jks.b64
# Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("breakee-release.jks")) > breakee-release.jks.b64
```

Pega el contenido de `.b64` en `KEYSTORE_BASE64`. Guarda el `.jks` y sus contraseñas fuera del
repo (si se pierden no se puede volver a firmar con la misma identidad). Con `gh`:

```bash
gh secret set KEYSTORE_BASE64 < breakee-release.jks.b64
gh secret set KEYSTORE_PASSWORD
gh secret set KEY_ALIAS --body breakee
gh secret set KEY_PASSWORD
```

**Google Sign-In:** añade el SHA-1 y SHA-256 del keystore de release en Firebase
(Configuración del proyecto → tu app Android) o el login con Google fallará en la release:

```bash
keytool -list -v -keystore breakee-release.jks -alias breakee
```

### R8 / minify

`isMinifyEnabled` está en `false` en release porque la app no se ha probado minificada en un
dispositivo. La build con R8 **sí compila** (APK de ~3,5 MB frente a ~18,7 MB) y las reglas para Hilt,
Firebase/Firestore, modelos de dominio, OkHttp y org.json están en `app/proguard-rules.pro`. Para
probarla: `ENABLE_R8=true ./gradlew assembleRelease` (o `-PENABLE_R8=true`); en CI se puede añadir
`ENABLE_R8: "true"` al `env` del job.
