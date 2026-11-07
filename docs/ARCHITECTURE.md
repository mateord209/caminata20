# Arquitectura y reglas SOLID — Breakee

Este documento es el contrato de diseño del proyecto. Se aplica a todo código nuevo y a
cualquier refactor que toque una HU. No es un objetivo estético: busca que las HU se puedan
probar sin Firebase, sin emulador y sin depender del reloj del dispositivo.

## 1. Dirección de dependencias

```
ui (Compose + ViewModels)  ──────►  domain  ◄──────  data (Firestore, DataStore, AlarmManager)
                                        ▲
                                        │
                                       di (Hilt)
```

- `domain` es Kotlin puro: **no** importa `android.*`, `com.google.firebase.*`, `androidx.datastore.*`
  ni clases de `data` o `ui`.
- `data` implementa las interfaces de `domain`; es la única capa que conoce Firebase, DataStore,
  AlarmManager y los tipos de Android.
- `ui` depende de `domain` (casos de uso, modelos, interfaces). No importa `data` directamente,
  salvo en los módulos de Hilt.
- `di` es el único lugar que decide qué implementación concreta se inyecta.

## 2. Responsabilidades por paquete

| Paquete | Responsabilidad |
|---|---|
| `domain/model` | Modelos inmutables y errores de dominio. Sin lógica de infraestructura. |
| `domain/repository` | Interfaces pequeñas de persistencia/servicios. |
| `domain/usecase` | Orquestación y reglas de negocio. Funciones puras cuando sea posible. |
| `domain/policy` | Reglas puras y aisladas (selección, evaluación de días, etc.). |
| `data/local` | DataStore, catálogos locales, stores de estado. |
| `data/remote` | Firestore, mapeos, rutas de documentos. |
| `data/repository` | Implementaciones de las interfaces de `domain`. |
| `ui/**` | Composables stateless + ViewModels que exponen estado y eventos. |
| `notifications` | Construcción y emisión de notificaciones; sin reglas de negocio. |

## 3. SOLID aplicado

- **SRP:** una clase cambia por una sola razón. Validar un correo, mapear un error de Firebase,
  calcular una racha y construir una notificación son responsabilidades distintas.
- **OCP:** las reglas nuevas se agregan con una implementación o una rama explícita en una policy,
  no con `if` dentro de `PauseNotifier`, `HomeViewModel` o un repositorio gigante.
- **LSP:** los fakes de test implementan exactamente las mismas interfaces y respetan el mismo
  contrato que Firestore/DataStore; no lanzan `UnsupportedOperationException`.
- **ISP:** interfaces pequeñas y cohesivas (`PhraseRepository`, `MeetingDayRepository`,
  `PhraseStateStore`). No se engorda `LocalStateRepository` con responsabilidades nuevas.
- **DIP:** `Clock`, `Random`, repositorios y casos de uso se inyectan por constructor. El dominio
  no llama `System.currentTimeMillis()` ni `Calendar.getInstance()` directamente.

### Regla anti-sobreingeniería (YAGNI)

Una interfaz se crea solo cuando existe una costura real: persistencia, tiempo, aleatoriedad,
notificación o una segunda implementación para tests. No se crea una interfaz por cada clase.

## 4. Convenciones

- Casos de uso: clase inyectable con `operator fun invoke(...)` o función pura; nunca un `object`
  con estado global o reloj escondido.
- Repositorios: interfaz en `domain/repository`, implementación en `data/repository`.
- Errores: `sealed class`/`sealed interface` en `domain/model`; el mapeo desde Firebase vive en
  `data`.
- Tiempo y aleatoriedad: `Clock` y `Random` inyectados; en tests se usan fijos.
- Modelos de UI: `data class` inmutables con valores por defecto; los Composables reciben estado
  y callbacks, no ViewModels.
- Nombres: `HU-XX` en el commit y en el PR; los tests citan el CA que cubren.

## 5. Pruebas

| Nivel | Herramienta | Qué cubre |
|---|---|---|
| Unitario | JUnit 4 + coroutines-test | Dominio, políticas, mapeos, ViewModels con fakes |
| UI | Compose UI Test (`androidTest`) | Pantallas stateless, errores inline, tarjeta de racha |
| E2E | Maestro | Login, navegación y smoke de la app real |
| Manual/QA | Emulador + screenshots | Notificaciones, temas, logo, accesibilidad |
| Datos | Firebase MCP | `users/{uid}`, `pauseHistory`, `meetingDays` |

- Los tests unitarios **no** llaman a Firebase ni a un emulador.
- Los fakes viven en `app/src/test/java/.../fakes`.
- Un PR no se considera terminado si no incluye tests del CA que dice cubrir.

## 6. Git flow

```
main  ←── PR de cierre de sprint ──  develop  ←── PR ──  feature/hu-XX-descripcion
                                      ↑                  fix/descripcion
                                      └────────────────  docs/descripcion
```

- `main`: releases estables y tags `v*`.
- `develop`: integración del sprint.
- Ramas cortas: `feature/`, `fix/`, `docs/`, `chore/`.
- Commits convencionales: `feat(hu-10): ...`, `test(hu-17): ...`, `fix(hu-10): ...`.
- PRs de rama corta hacia `develop`; al cierre, `develop` → `main`.
- CI: tests + build en cada PR a `develop`/`main`; prerelease desde `develop`.

## 7. Definition of Done

Una HU está terminada cuando:

1. Todos sus CA tienen evidencia (test, screenshot o verificación manual).
2. `testDebugUnitTest` y `assembleDebug` pasan.
3. El PR tiene la checklist SOLID completa.
4. No hay imports de `data`/Android en `domain`.
5. ClickUp está actualizado con estado, evidencia y enlace al PR.
6. La documentación de cierre refleja el estado real.

## 8. Deuda técnica conocida (fuera de alcance de este sprint)

- `LocalStateRepository` es un god object; no se amplía con frases ni días de reunión.
- `UserPaths` mantiene su propio listener de auth además de `FirebaseAuthRepository`.
- `PauseNotifier` usa `ExerciseCatalog` directamente; se resolverá con un `ExerciseRepository`
  en una iteración futura.
- `domain/usecase/PauseUseCases.kt` y `SuggestedExercise.kt` importan `data.local.ExerciseCatalog`
  (deuda previa). No se tocó para no mezclar el refactor con las HU de este sprint; el código nuevo
  de HU-10/HU-07/HU-17 no agrega imports de `data` en `domain`.
- `pauseHistory` limita a 500 registros; la racha máxima calculable es ~62 días con meta 8.
- No hay tests instrumentados previos; se crea el source set `androidTest` en este sprint.
