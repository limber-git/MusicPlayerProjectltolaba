# Release Checklist

## 1) Versionado

Definir antes del build:

- `APP_VERSION_CODE` (entero incremental)
- `APP_VERSION_NAME` (ej. `1.2.0`)

## 2) Keystore y firma

Configurar:

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

En CI se usa `RELEASE_STORE_FILE_BASE64` + secretos de contrase?a/alias.

## 3) Quality gates

Ejecutar:

```bash
./gradlew lint
./gradlew test
./gradlew assembleProdRelease
```

## 4) Artefacto

APK release:

- `app/build/outputs/apk/prod/release/app-prod-release.apk`

## 5) Firma y publicaci?n

- Confirmar que `assembleProdRelease` use keystore real (no debug).
- Subir a canal interno / Play Console seg?n estrategia del equipo.

## 6) Verificaci?n m?nima en dispositivo

- Reproducir pista local.
- Bloquear pantalla y validar controles multimedia.
- Cambiar velocidad/tonalidad.
- Ajustar ecualizador y confirmar efecto audible.
- Cerrar app y validar reproducci?n en segundo plano.
