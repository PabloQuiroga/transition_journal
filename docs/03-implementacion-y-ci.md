# Capa 3: Implementación, Testing Unitario y CI/CD
**Proyecto:** Diario Íntimo de Transición y Expresión (Nombre clave: *Refugio*)  
**Versión:** 1.0  
**Estado:** Listo para inicialización de repositorio  

---

## 1. Estructura del Repositorio y Paquetes

Al crear el proyecto en Android Studio (Empty Activity con Jetpack Compose) y subirlo a GitHub, la estructura recomendada es la siguiente:

```text
refugio-android/
├── .github/
│   └── workflows/
│       └── android-ci.yml          # Pipeline de verificación automática
├── docs/
│   ├── 01-especificacion.md        # Capa 1: Historias de usuario y reglas
│   ├── 02-diseno-logico.md         # Capa 2: Arquitectura, modelos y contratos
│   └── 03-implementacion-y-ci.md   # Capa 3: Stack, tests y CI (este archivo)
├── app/
│   └── src/
│       ├── main/
│       │   ├── java/com/refugio/journal/
│       │   │   ├── core/
│       │   │   │   ├── security/   # Keystore, Biometría, FLAG_SECURE
│       │   │   │   ├── storage/    # PrivatePhotoStorage, FileProvider helper
│       │   │   │   └── theme/      # Color, Type, Theme (Material 3)
│       │   │   ├── data/
│       │   │   │   ├── local/      # Room Database, Entities, DAOs
│       │   │   │   └── repository/ # Implementaciones de repositorios
│       │   │   ├── domain/
│       │   │   │   ├── model/      # JournalEntry, EntryPhoto, Mood, Category
│       │   │   │   ├── repository/ # Interfaces puras
│       │   │   │   └── usecase/    # Reglas de negocio aisladas
│       │   │   └── ui/
│       │   │       ├── lock/       # PinSetupScreen, LockScreen, LockViewModel
│       │   │       ├── timeline/   # TimelineScreen, FilterChips, TimelineViewModel
│       │   │       ├── editor/     # EntryEditorScreen, PhotoCarousel, EditorViewModel
│       │   │       ├── detail/     # EntryDetailScreen, ShareBottomSheet
│       │   │       └── navigation/ # AppNavHost y manejo de estado de sesión
│       │   └── res/
│       │       └── xml/
│       │           └── file_paths.xml  # Rutas permitidas para FileProvider (solo cache)
│       └── test/                   # Tests Unitarios (JVM) - Rápidos, sin emulador
│           └── java/com/refugio/journal/
│               ├── domain/usecase/ # Tests de reglas de negocio
│               ├── data/local/     # Tests de Room DAO con Robolectric
│               └── ui/             # Tests de ViewModels y StateFlow con Turbine
└── build.gradle.kts
```

---

## 2. Stack Técnico y Dependencias (`libs.versions.toml`)

*   **Lenguaje:** Kotlin `2.0+`
*   **UI:** Jetpack Compose (BOM reciente) + Material 3 + Navigation Compose
*   **Inyección de Dependencias:** Hilt (`com.google.dagger:hilt-android`)
*   **Persistencia Local:**
    *   Room (`androidx.room:room-runtime`, `room-ktx`, `room-compiler` con KSP)
    *   Cifrado opcional de DB: SQLCipher (`net.zetetic:sqlcipher-android`)
*   **Seguridad y Privacidad:**
    *   `androidx.security:security-crypto` (`EncryptedSharedPreferences` para hash de PIN y preferencias)
    *   `androidx.biometric:biometric` (Autenticación nativa con huella/rostro)
*   **Imágenes:** Coil 3 (`io.coil-kt.coil3:coil-compose`)
*   **Testing Unitario (Solo `testImplementation`):**
    *   JUnit 5 (`org.junit.jupiter:junit-jupiter`) o JUnit 4
    *   MockK (`io.mockk:mockk`) para simular repositorios y almacenamiento
    *   Coroutines Test (`org.jetbrains.kotlinx:kotlinx-coroutines-test`)
    *   Turbine (`app.cash.turbine:turbine`) para aserciones limpias sobre `StateFlow`
    *   Robolectric (`org.robolectric:robolectric`) + `androidx.room:room-testing` para probar queries SQLite en JVM

---

## 3. Estrategia de Testing Unitario

Para cumplir con los criterios de aceptación de la **Capa 1**, cada componente crítico debe contar con cobertura unitaria automatizada:

### 3.1. Tests de Capa de Dominio (`UseCases`)
*   `CreateOrUpdateEntryUseCaseTest`:
    *   Verifica que rechace entradas sin texto ni fotos (`AC-03.2`).
    *   Verifica que genere el título automático con la fecha si se deja vacío (`AC-03.3`).
    *   Verifica que conserve `createdAt` y actualice `updatedAt` en ediciones (`AC-05.1`).
*   `DeleteEntryUseCaseTest`:
    *   Verifica que al eliminar una entrada con 3 fotos, se invoquen 3 borrados físicos en `PhotoStorageManager` antes/después de borrar en DB (`RN-03`).
*   `VerifyAccessUseCaseTest`:
    *   Verifica validación de PIN correcto/incorrecto y activación de bloqueo temporal tras 5 intentos fallidos.

### 3.2. Tests de Capa de Presentación (`ViewModels`)
*   `TimelineViewModelTest` (usando **Turbine**):
    *   Verifica que al aplicar filtros de `Category.EXPRESION_ESTILO` o `onlyMilestones = true`, el `UiState` emita exactamente el subconjunto esperado (`AC-06.1`, `AC-06.2`).
*   `LockViewModelTest`:
    *   Verifica la transición de `OnboardingPin` -> `Unlocked` y que un mismatch en la confirmación del PIN emita error sin persistir (`AC-01.2`).

### 3.3. Tests de Capa de Datos (`Room DAOs`)
*   `JournalDaoTest` (usando **Robolectric** + `Room.inMemoryDatabaseBuilder`):
    *   Verifica integridad referencial (`ForeignKey` con `CASCADE` al borrar entradas con fotos) y ordenamiento cronológico descendente.

---

## 4. Integración Continua con GitHub Actions

Archivo a crear en `.github/workflows/android-ci.yml` en la raíz del repositorio. Este pipeline se ejecuta automáticamente en cada `push` o `Pull Request` hacia `main` y `develop`:

```yaml
name: Android CI - Build & Unit Tests

on:
  push:
    branches: [ "main", "develop" ]
  pull_request:
    branches: [ "main", "develop" ]

jobs:
  verify:
    name: Lint, Unit Tests & Debug APK
    runs-on: ubuntu-latest

    steps:
      - name: Checkout del repositorio
        uses: actions/checkout@v4

      - name: Configurar JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: gradle

      - name: Dar permisos de ejecución a Gradle
        run: chmod +x gradlew

      - name: Ejecutar Android Lint
        run: ./gradlew lintDebug --stacktrace

      - name: Ejecutar Tests Unitarios (JVM)
        run: ./gradlew testDebugUnitTest --stacktrace

      - name: Compilar APK de Debug (Verificación de Build)
        run: ./gradlew assembleDebug --stacktrace

      - name: Subir reporte de tests en caso de fallo
        if: always()
        uses: actions/upload-artifact@v4
        with:
          name: unit-test-reports
          path: app/build/reports/tests/testDebugUnitTest/
          retention-days: 7
```

---

## 5. Checklist de Arranque del Proyecto (Primer Commit)

1. Crear repositorio privado en GitHub (ej. `refugio-journal-android`).
2. Inicializar proyecto en Android Studio con paquete base `com.refugio.journal` y `minSdk = 26`.
3. Verificar que `AndroidManifest.xml` **no** contenga `<uses-permission android:name="android.permission.INTERNET" />`.
4. Añadir `window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)` en `MainActivity.onCreate()`.
5. Guardar los 3 documentos en `/docs` y el pipeline en `.github/workflows/android-ci.yml`.
6. Hacer el primer `push` y confirmar que el workflow de GitHub Actions pase en verde.
```

---

Con este documento ya tenés el paquete completo dividido en sus tres capas (**Especificación**, **Diseño Lógico** e **Implementación + Tests + CI**). 

Cuando crees el proyecto en Android Studio y el repositorio en GitHub, si querés podemos ir bajando esto a código real paso a paso (por ejemplo, armando primero el módulo de seguridad y bloqueo o la base de datos con sus tests unitarios). ¿Cómo venís con eso?