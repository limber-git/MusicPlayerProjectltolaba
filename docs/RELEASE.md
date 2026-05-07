# Guía de Lanzamiento (Release)

Este documento detalla los pasos necesarios para generar y validar una versión de producción de HexaMusicPlayer.

## 1. Configuración de Versión

Antes de compilar, asegúrate de actualizar los siguientes valores en `app/build.gradle.kts` o mediante propiedades de Gradle:

- `APP_VERSION_CODE`: Un entero incremental.
- `APP_VERSION_NAME`: El nombre semántico de la versión (ej. `1.0.0`).

## 2. Firma de la Aplicación

Para compilaciones de producción, es necesario configurar las claves de firma mediante variables de entorno o secretos en CI/CD:

- `RELEASE_STORE_FILE`: Ruta al archivo keystore.
- `RELEASE_STORE_PASSWORD`: Contraseña del almacén de claves.
- `RELEASE_KEY_ALIAS`: Alias de la clave.
- `RELEASE_KEY_PASSWORD`: Contraseña de la clave.

## 3. Control de Calidad

Ejecuta la suite completa de verificaciones antes de generar el artefacto:

```bash
./gradlew lint
./gradlew test
./gradlew assembleProdRelease
```

## 4. Validación en Dispositivo

Una vez generado el APK en `app/build/outputs/apk/prod/release/`, se debe verificar manualmente:

1. **Biblioteca:** Correcto escaneo de pistas locales.
2. **Reproducción:** Funcionamiento estable en primer y segundo plano.
3. **Controles:** Validación de la notificación multimedia y controles de pantalla de bloqueo.
4. **Audio Studio:** Confirmar que el ecualizador y los efectos de audio se aplican correctamente al motor.
5. **Persistencia:** Validar que los favoritos y ajustes se mantienen tras reiniciar la aplicación.
