# HexaMusicPlayer

Reproductor de música nativo para Android (Kotlin + Jetpack Compose) con arquitectura hexagonal, biblioteca local, reproducción avanzada y procesamiento de audio.

## Características

- **Reproducción Local:** Escaneo automático de música mediante `MediaStore`.
- **Audio de Alta Fidelidad:** Motor basado en Media3 ExoPlayer con soporte para reproducción en segundo plano y controles de sesión multimedia.
- **Audio Studio:** Control granular de velocidad, tonalidad (pitch), ecualizador de 10 bandas, refuerzo de bajos, virtualizador 3D y potenciador de ganancia.
- **UI/UX Moderna:** Interfaz fluida construida íntegramente con Jetpack Compose y Material 3.
- **Arquitectura Limpia:** Separación estricta de responsabilidades siguiendo el patrón de puertos y adaptadores.

## Estructura del Proyecto (Arquitectura Hexagonal)

- `domain`: Contiene la lógica de negocio pura (modelos, puertos/interfaces y casos de uso).
- `infrastructure`: Implementaciones técnicas de los puertos (reproductor, base de datos, efectos de audio, preferencias).
- `ui`: Capa de presentación reactiva con Compose y ViewModels.

## Requisitos y Configuración

- JDK 17 o superior.
- Android SDK (Nivel de API 26+).
- Android Studio Ladybug o superior recomendado.

## Comandos de Desarrollo

Ejecutar análisis de código y pruebas:
```bash
./gradlew lint test
```

Generar APK de depuración:
```bash
./gradlew assembleDevDebug
```

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.
