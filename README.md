# studioMusicTolaba

Reproductor de musica nativo para Android hecho con Kotlin, Jetpack Compose y Media3. La vision del proyecto es combinar una experiencia de reproduccion moderna, inspirada por apps como Spotify y YouTube Music, con un modo estudio pensado para practicar, analizar canciones y acompanar instrumentos.

## Funciones actuales

- Biblioteca local mediante `MediaStore`.
- Reproduccion con Media3 ExoPlayer.
- Cola de reproduccion, favoritos, recientes, shuffle y repeat.
- Controles de reproduccion en segundo plano mediante servicio foreground y MediaSession.
- Modo Studio con speed, pitch, ecualizador de 10 bandas, bass boost, virtualizer y loudness.
- Preferencias persistentes para tema, idioma, fuente musical y ajustes de audio.

## En progreso

- Reestructuracion visual del reproductor y del modo Studio.
- Widget de inicio y experiencia de lockscreen/notificacion mas completa.
- Internacionalizacion total en espanol e ingles.
- Analisis musical del modo Studio: nota, tonalidad, cambios armonicos y vistas por instrumento.
- Preparacion para publicacion futura en Google Play.

## Requisitos

- JDK 17 o superior.
- Android Studio Ladybug o superior recomendado.
- Android SDK con API 35 para builds compatibles con requisitos actuales de Google Play.

## Comandos

Ejecutar pruebas y lint:

```bash
./gradlew lint test
```

Generar APK instalable de desarrollo:

```bash
./gradlew assembleDevDebug
```

Generar build de produccion:

```bash
./gradlew assembleProdRelease
```

## Documentacion

- [Guia de lanzamiento](docs/RELEASE.md)
- [Checklist Google Play](docs/PLAY_STORE_CHECKLIST.md)
- [Arquitectura Studio Pro](docs/STUDIO_PRO_ARCHITECTURE.md)
- [Backlog de rendimiento](docs/PERFORMANCE_BACKLOG.md)

## Licencia

MIT. Consulta [LICENSE](LICENSE).
