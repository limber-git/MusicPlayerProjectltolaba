# Guia de lanzamiento

Pasos para generar y validar una version de studioMusicTolaba.

## Version

Configurar antes del build:

- `APP_VERSION_CODE`: entero incremental.
- `APP_VERSION_NAME`: version visible, por ejemplo `1.0.0`.

Se pueden definir como propiedades Gradle o variables de entorno.

## Firma

Para builds de produccion, configurar:

- `RELEASE_STORE_FILE`: ruta al keystore.
- `RELEASE_STORE_PASSWORD`: password del keystore.
- `RELEASE_KEY_ALIAS`: alias de la clave.
- `RELEASE_KEY_PASSWORD`: password de la clave.

En GitHub Actions estos valores deben ir como secrets. El keystore puede guardarse como base64 en `RELEASE_STORE_FILE_BASE64`, siguiendo el workflow actual.

## Calidad

Ejecutar antes de publicar:

```bash
./gradlew lint
./gradlew test
./gradlew assembleProdRelease
./gradlew bundleProdRelease
```

## Validacion manual

Revisar en un telefono real:

1. Biblioteca local y permisos.
2. Reproduccion en foreground, background y pantalla bloqueada.
3. Notificacion multimedia y controles.
4. Seek bar, cola `Up Next`, shuffle, repeat y favoritos.
5. Modo Studio: speed, pitch, EQ y efectos.
6. Cambio de idioma y tema.
7. Persistencia tras cerrar y abrir la app.

## Artefactos

- APK debug: `app/build/outputs/apk/dev/debug/`
- APK release: `app/build/outputs/apk/prod/release/`
- AAB release para Play: `app/build/outputs/bundle/prodRelease/`

Para Google Play, priorizar el `.aab`.
