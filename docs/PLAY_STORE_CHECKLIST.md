# Checklist Google Play para studioMusicTolaba

Este documento reune los puntos que debemos cerrar antes de publicar la app en Google Play. Las politicas cambian, asi que este archivo debe revisarse antes de cada release importante.

## Estado tecnico

- `applicationId`: `com.tolaba.studiomusic`.
- `minSdk`: 26.
- `targetSdk`: 36.
- `compileSdk`: 36.
- Firma release: pendiente de keystore definitivo y secretos de CI.
- Formato recomendado para Play: generar `AAB` con `./gradlew bundleProdRelease`.

## Requisitos de build

- Google Play exige que nuevas apps y actualizaciones apunten a Android 15 / API 35 o superior.
- Mantener Android Gradle Plugin, Kotlin, Media3, Compose y dependencias actualizadas antes de publicar.
- Ejecutar antes de subir:

```bash
./gradlew lint test assembleProdRelease bundleProdRelease
```

## Privacidad y permisos

La app usa permisos sensibles para biblioteca local y reproduccion:

- `READ_MEDIA_AUDIO`
- `READ_EXTERNAL_STORAGE` hasta Android 12
- `MODIFY_AUDIO_SETTINGS`
- `POST_NOTIFICATIONS`
- `FOREGROUND_SERVICE`
- `FOREGROUND_SERVICE_MEDIA_PLAYBACK`

Antes de Play Store necesitamos:

- Politica de privacidad publica en una URL activa.
- Link a la politica dentro de la app.
- Declaracion Data Safety coherente con permisos, SDKs y comportamiento real.
- Explicar que la app lee audio local para construir la biblioteca y reproducir archivos.
- Confirmar si hay o no coleccion, transferencia o comparticion de datos.
- Si agregamos analytics, crash reporting, ads, login o cloud sync, actualizar Data Safety y privacidad.

## Ficha de Play Store

Pendiente preparar:

- Nombre: `studioMusicTolaba`.
- Descripcion corta.
- Descripcion larga.
- Categoria: Music & Audio.
- Icono hi-res 512 x 512.
- Feature graphic 1024 x 500.
- Minimo 2 screenshots de telefono.
- Screenshots tablet si buscamos buena presencia en tablets.
- Video promocional opcional.
- Email de soporte.
- Sitio web opcional.

## App content

Completar en Play Console:

- Privacy Policy.
- Data Safety.
- Ads: declarar si contiene o no anuncios.
- Content Rating.
- Target audience and content.
- News app: declarar que no aplica.
- Government apps: declarar que no aplica.
- Health apps: declarar que no aplica.
- Financial features: declarar que no aplica si no agregamos pagos, cripto, prestamos o banco.

## Calidad de producto

Antes de publicar debemos validar:

- Reproduccion en primer plano, segundo plano y pantalla bloqueada.
- Controles de notificacion: anterior, play/pausa, siguiente y detener.
- Audio focus ante llamadas, otras apps de musica y auriculares Bluetooth.
- Cola `Up Next` con listas largas.
- Seek bar interactiva.
- Biblioteca con permisos concedidos, denegados y revocados.
- Cambio de idioma espanol/ingles.
- Tema claro/oscuro/sistema.
- Modo Studio con y sin pista activa.
- Ecualizador en pantallas pequenas.
- App sin crasheos al rotar pantalla o volver desde background.

## Modo Studio Pro

Para las funciones de nota, tonalidad y acordes necesitamos una etapa tecnica nueva:

- Analizador de pitch por ventana temporal.
- Estimador de tonalidad global.
- Estimador de acordes/cambios armonicos.
- Timeline de eventos musicales sincronizado con la cancion.
- Capas por instrumento: teclado, guitarra, saxo, flauta y otros.
- Cache local de analisis por pista.
- UI para corregir manualmente notas/acordes cuando el analisis automatico falle.

## Fuentes oficiales

- Target API level: https://developer.android.com/google/play/requirements/target-sdk
- Data Safety: https://support.google.com/googleplay/android-developer/answer/10787469
- App content and privacy policy: https://support.google.com/googleplay/android-developer/answer/9859455
- User Data policy: https://support.google.com/googleplay/android-developer/answer/10144311
- Preview assets: https://support.google.com/googleplay/android-developer/answer/1078870
- Core app quality: https://developer.android.com/docs/quality-guidelines/core-app-quality
