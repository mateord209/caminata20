## Resumen

<!-- Qué cambia y por qué. -->

## HU / CA

- HU:
- CA cubiertos:

## Checklist SOLID

- [ ] Cada clase nueva tiene una responsabilidad clara.
- [ ] `domain` no importa `data`, `ui`, `android.*` ni Firebase.
- [ ] Las interfaces son pequeñas y justificadas (sin una interfaz por clase).
- [ ] Las dependencias se inyectan por constructor (`Clock`, `Random`, repositorios).
- [ ] No se amplió `LocalStateRepository` con responsabilidades nuevas.
- [ ] Los tests usan fakes; no llaman a Firebase ni a un emulador.

## Pruebas

- [ ] `./gradlew testDebugUnitTest`
- [ ] `./gradlew assembleDebug`
- [ ] `./gradlew connectedDebugAndroidTest` (si aplica)
- [ ] Maestro (si aplica)
- [ ] Evidencia/screenshot adjunto

## Riesgos y deuda

<!-- Qué queda pendiente y por qué. -->
