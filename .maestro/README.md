# Flujos Maestro

Requisitos: Maestro CLI 2.x y un emulador/dispositivo conectado.

```bash
maestro test .maestro/login-validation.yaml
TEST_EMAIL=... TEST_PASSWORD=... maestro test .maestro/streak.yaml
```

- `login-validation.yaml` no necesita cuenta: limpia el estado, omite el onboarding y verifica
  los errores inline del login.
- `streak.yaml` usa una cuenta de prueba existente y verifica la tarjeta “Racha actual” del
  Dashboard. No commitees credenciales; usa variables de entorno o secretos de CI.

En Windows, si Maestro no está en el PATH:

```powershell
& "C:\maestro\maestro\bin\maestro.bat" test .maestro\login-validation.yaml
```
