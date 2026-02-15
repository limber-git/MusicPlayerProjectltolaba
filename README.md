# HexaMusicPlayer

Android music player nativo (Kotlin + Jetpack Compose) con arquitectura hexagonal, biblioteca local, reproducci?n avanzada y procesamiento de audio.

## Caracter?sticas

- Reproducci?n de m?sica local con MediaStore.
- Reproducci?n en segundo plano con Media3 `MediaSessionService`.
- Ecualizador avanzado por bandas.
- Controles de audio: velocidad, tonalidad, bass boost, virtualizer, loudness.
- UI/UX moderna en Compose.
- Flavors: `dev` y `prod`.
- Build de `release` con R8 + resource shrinking.

## Stack t?cnico

- Kotlin 1.9+
- Android Gradle Plugin 8.1.1
- Jetpack Compose
- Media3 ExoPlayer + Session
- Coroutines + StateFlow

## Estructura (hexagonal)

- `app/src/main/java/com/limbe/hexamusicplayer/domain`
  - `model`: entidades de dominio
  - `port`: interfaces (puertos)
  - `usecase`: casos de uso
- `app/src/main/java/com/limbe/hexamusicplayer/infrastructure`
  - `mediastore`: acceso a m?sica local
  - `player`: adaptador ExoPlayer
  - `effects`: efectos de audio Android
  - `session`: administraci?n de MediaSession
  - `service`: `PlaybackMediaSessionService`
- `app/src/main/java/com/limbe/hexamusicplayer/ui`
  - pantallas Compose, ViewModel, theme

## Requisitos

- JDK 17
- Android SDK instalado
- Variables de entorno Android configuradas (Android Studio o CLI)

## Comandos locales

Desde la ra?z del proyecto:

```bash
./gradlew lint
./gradlew test
./gradlew assembleDevDebug
./gradlew assembleProdRelease
```

En Windows (PowerShell/CMD):

```powershell
.\gradlew.bat lint
.\gradlew.bat test
.\gradlew.bat assembleDevDebug
.\gradlew.bat assembleProdRelease
```

## APKs generados

- Dev debug: `app/build/outputs/apk/dev/debug/app-dev-debug.apk`
- Prod release: `app/build/outputs/apk/prod/release/app-prod-release.apk`

## Versionado

`app/build.gradle.kts` acepta override por Gradle property o variables de entorno:

- `APP_VERSION_CODE`
- `APP_VERSION_NAME`

Ejemplo:

```bash
./gradlew assembleProdRelease -PAPP_VERSION_CODE=25 -PAPP_VERSION_NAME=1.4.0
```

## Firma de release

El build `release` soporta firma por variables (propiedades o env vars):

- `RELEASE_STORE_FILE`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

Si no se definen, se usa firma debug para facilitar validaci?n local.
Para publicaci?n real, definir los 4 valores.

## CI/CD (GitHub Actions)

Workflow: `.github/workflows/android-ci.yml`

- Push/PR:
  - `lint`
  - `test`
  - `assembleDevDebug`
  - artifact: `dev-debug-apk`
- Manual (`workflow_dispatch`) o tag:
  - build `assembleProdRelease`
  - artifact: `prod-release-apk`

Secrets requeridos para release firmado en CI:

- `RELEASE_STORE_FILE_BASE64`
- `RELEASE_STORE_PASSWORD`
- `RELEASE_KEY_ALIAS`
- `RELEASE_KEY_PASSWORD`

## Subir a Git

```bash
git init
git add .
git commit -m "feat: production-ready Android music player"
git branch -M main
git remote add origin <TU_URL_REPO>
git push -u origin main
```

## Notas

- `lint.xml` est? ajustado al entorno actual para evitar falsos positivos de checks obsoletos.
- `proguard-rules.pro` est? listo para ampliar reglas si se agrega librer?a con reflexi?n.
