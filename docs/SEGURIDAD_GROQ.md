# Riesgo de `GROQ_API_KEY` en el APK y propuesta de proxy

## Situación actual
`app/build.gradle.kts` lee `GROQ_API_KEY` de `local.properties` y lo inyecta como
`BuildConfig.GROQ_API_KEY`. Ese valor queda **en texto plano dentro del APK** (clase `BuildConfig`
en `classes.dex`).

## Riesgo
- Cualquiera con el APK (Play Store, un APK compartido o un dispositivo rooteado) puede extraer la
  clave en minutos con `apktool`, `jadx` o `strings`. La ofuscación con R8 **no** la protege porque
  es una constante literal.
- Con la clave robada se puede consumir la cuenta de Groq del equipo, agotar la cuota o generar costos,
  hacer peticiones a nombre del proyecto y saltarse cualquier límite que ponga la app.
- Rotar la clave obliga a publicar una nueva versión de la app, y las versiones viejas dejan de funcionar.
- `local.properties` no se versiona, pero sí viaja en cada APK que se comparte.

**Recomendación inmediata:** si algún APK con la clave ya salió del equipo, rotarla en la consola de Groq.

## Propuesta: proxy en el backend
```
App (Compose) ── HTTPS + Firebase ID token ──▶ Proxy (Cloud Function / Cloud Run)
                                                 │ verifica ID token (+ App Check)
                                                 │ aplica rate-limit por uid
                                                 └─▶ api.groq.com (GROQ_API_KEY en Secret Manager)
```
1. La clave vive solo en el servidor (Secret Manager), nunca en el cliente.
2. El proxy exige `Authorization: Bearer <Firebase ID token>` y lo verifica con Firebase Admin. Así solo
   los usuarios autenticados de la app pueden usarlo.
3. Opcional: **Firebase App Check** (Play Integrity) para rechazar clientes que no sean la app oficial.
4. Límite por usuario (p. ej. 20 rutinas al día, contadas en Firestore) y un modelo y un prompt fijos
   del lado del servidor, para que el proxy no se convierta en un Groq abierto.
5. Registro de uso por uid para auditoría.

Hay un ejemplo de Cloud Function (Node 20, 2.ª gen.) en `docs/groq-proxy/index.js`.

## Soporte en la app (ya implementado)
- `GroqConfig.proxyUrl` sale de `GROQ_PROXY_URL` en `local.properties`.
- Si `GROQ_PROXY_URL` está definido, `GroqExerciseGenerator` llama al proxy con el **ID token de Firebase**
  y **no usa** `GROQ_API_KEY`. En ese caso conviene dejar `GROQ_API_KEY` vacío para que no quede en el APK.
- Si no está definido, se mantiene el comportamiento actual (llamada directa) para no romper el
  desarrollo local.

## Pasos para activar el proxy
1. `firebase init functions` y copiar `docs/groq-proxy/index.js`.
2. `firebase functions:secrets:set GROQ_API_KEY`.
3. `firebase deploy --only functions:groqProxy`.
4. En `local.properties`: `GROQ_PROXY_URL=https://<región>-<proyecto>.cloudfunctions.net/groqProxy` y
   borrar `GROQ_API_KEY`.
5. Rotar la clave anterior de Groq.
