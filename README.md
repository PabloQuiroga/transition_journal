# Diario de Transición y Expresión (Android Native)

Aplicación móvil nativa para Android, **100% offline-first**, diseñada como un espacio íntimo, seguro y privado para documentar el proceso de transición social, explorar la expresión de género y realizar seguimiento emocional, con opciones controladas para compartir momentos puntuales.

---

## 🎯 Propósito del Proyecto

El objetivo es ofrecer un refugio digital personal que funcione sin conexión a internet y sin servidores externos, garantizando que toda la información (textos, estados de ánimo y fotografías) permanezca exclusivamente en el dispositivo del usuario bajo estrictas medidas de privacidad y aislamiento.

### Características Principales (MVP)
* **Privacidad Total (Offline-First):** Sin permisos de red (`INTERNET`). Ningún dato sale del teléfono a menos que el usuario decida compartirlo explícitamente.
* **Seguridad y Bloqueo:** Acceso protegido mediante PIN cifrado y biometría nativa (`BiometricPrompt`). Bloqueo automático al minimizar la app (`ON_STOP`), botón de bloqueo rápido y protección contra capturas de pantalla (`FLAG_SECURE`).
* **Registro de Entradas:** Diario personal con título, nota libre, selector de estado emocional (*Eufórica, Empoderada, Tranquila, Vulnerable, Ansiosa*) y categorización (*Expresión y Estilo, Social, Emocional, Hito*).
* **Galería Privada Aislada:** Adjunto de fotografías almacenadas dentro del directorio interno privado de la aplicación (`filesDir`), evitando que aparezcan mezcladas en la galería pública del teléfono o en copias de seguridad en la nube.
* **Línea de Tiempo e Hitos:** Visualización cronológica con filtros por categoría, estado de ánimo o hitos destacados.
* **Compartir Controlado:** Posibilidad de exportar y compartir una entrada puntual (texto y/o foto mediante `FileProvider` temporal) con personas de confianza o profesionales de la salud, sin exponer el resto del diario.

---

## 📐 Metodología: Spec-Driven Development (SDD)

Este proyecto está estructurado en tres capas documentales que guían el desarrollo desde el requerimiento funcional hasta la implementación y automatización:

1. **[Capa 1: Especificación Funcional y Reglas de Negocio](docs/01-especificacion.md)**
   * Define el **QUÉ** y el **POR QUÉ**: historias de usuario, reglas de privacidad y criterios de aceptación redactados en formato Gherkin (*Given-When-Then*).
2. **[Capa 2: Diseño Lógico y Arquitectura](docs/02-diseno-logico.md)**
   * Define el **CÓMO conceptual**: arquitectura limpia (Clean Architecture + MVVM), modelos de dominio puros, contratos de repositorios, casos de uso y máquina de estados de seguridad.
3. **[Capa 3: Implementación, Testing y CI](docs/03-implementacion-y-ci.md)**
   * Define el **CON QUÉ**: estructura de paquetes en Android Studio, stack tecnológico, estrategia de testing unitario en JVM y pipeline de Integración Continua con GitHub Actions.

---

## 🏗️ Arquitectura y Stack Tecnológico

El sistema sigue los principios de **Clean Architecture** combinado con **MVVM / MVI** para desacoplar la lógica de negocio del framework de Android y maximizar la cobertura de pruebas unitarias:

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose + Material 3
* **Inyección de Dependencias:** Hilt / Koin
* **Persistencia Local:** Room (SQLite) + `EncryptedSharedPreferences`
* **Seguridad y Biometría:** Android Keystore + `androidx.biometric`
* **Manejo de Imágenes:** Coil + Android Photo Picker (`PickVisualMedia`)
* **Testing Unitario:** JUnit 5, MockK, Turbine (`StateFlow`), `kotlinx-coroutines-test` y Robolectric (para DAOs en memoria).
* **CI/CD:** GitHub Actions (Lint, Tests Unitarios, Cobertura y Build de APK Debug).

### Estructura del Repositorio

```text
├── .github/
│   └── workflows/
│       └── android-ci.yml          # Pipeline de CI (Lint, Unit Tests, Build)
├── docs/
│   ├── 01-especificacion.md        # Requerimientos, US y Criterios de Aceptación
│   ├── 02-diseno-logico.md         # Clean Architecture, Dominio y Máquina de Estados
│   └── 03-implementacion-y-ci.md   # Estructura Android, Estrategia de Tests y CI
├── app/
│   └── src/
│       ├── main/java/com/...
│       │   ├── core/               # Seguridad, Storage privado, Base de datos
│       │   ├── domain/             # Modelos puros, Interfaces y UseCases
│       │   └── ui/                 # Pantallas Compose, ViewModels y Navegación
│       └── test/java/com/...       # Tests unitarios (UseCases, ViewModels, DAOs)
└── README.md
```

## 🚀 Cómo empezar (Desarrollo Local)

1. **Clonar el repositorio:**
   ```bash
   git clone [https://github.com/PabloQuiroga/transition_journal.git](https://github.com/PabloQuiroga/transition_journal.git)
   cd transition_journal
    ```
2. Abrir en Android Studio:
   
    Se recomienda utilizar Android Studio Ladybug o superior con JDK 17.
    Sincronizar el proyecto con Gradle (Sync Project with Gradle Files).

4. Ejecutar los tests unitarios:
   ```bash
    ./gradlew testDebugUnitTest
    ```
    
5. Verificar reglas de estilo y Lint:
    ```bash
    ./gradlew lintDebug
    ```
    
