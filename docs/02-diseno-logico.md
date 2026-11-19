# Capa 2: Diseño Lógico y Arquitectura
**Proyecto:** Diario Íntimo de Transición y Expresión (Nombre clave: *Refugio*)  
**Versión:** 1.0  
**Estado:** Validado  

---

## 1. Arquitectura General (Clean Architecture + MVVM)

Para garantizar que el proyecto sea escalable, mantenible y **100% testeable mediante tests unitarios en JVM** (sin depender del emulador de Android para probar la lógica de negocio), el sistema se divide en tres capas concéntricas con regla de dependencia hacia adentro:

```text
┌──────────────────────────────────────────────────────────┐
│                 CAPA DE PRESENTACIÓN                     │
│  Jetpack Compose UI  ──▶  ViewModels (MVI / StateFlow)   │
└────────────────────────────┬─────────────────────────────┘
                             │ Depende de
                             ▼
┌──────────────────────────────────────────────────────────┐
│                   CAPA DE DOMINIO (Core)                 │
│  Modelos Puros │ Casos de Uso (UseCases) │ Interfaces    │
│           (100% Kotlin puro - Sin Android SDK)           │
└────────────────────────────▲─────────────────────────────┘
                             │ Implementa interfaces de
┌────────────────────────────┴─────────────────────────────┐
│                     CAPA DE DATOS                        │
│  Repositorios │ Room DB (SQLite) │ FileStorage │ Security│
└──────────────────────────────────────────────────────────┘
```

*   **Domain (`domain`):** Contiene los modelos de negocio, las reglas de validación y las interfaces de los repositorios. Al ser Kotlin puro, los tests unitarios corren en milisegundos.
*   **Data (`data`):** Implementa las interfaces definidas en `domain`. Gestiona la persistencia en base de datos, el copiado/borrado de fotos en el almacenamiento privado y el cifrado del PIN.
*   **Presentation (`ui`):** Observa estados inmutables (`UiState`) expuestos por los `ViewModels` mediante `StateFlow` y emite eventos de usuario (`UiEvent`).

---

## 2. Modelo de Dominio (Entidades Puras)

Modelos independientes de las anotaciones de base de datos o serialización:

```kotlin
data class JournalEntry(
    val id: Long = 0L,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val occurredAt: Instant = createdAt,
    val title: String,
    val content: String,
    val mood: Mood? = null,
    val category: Category,
    val isMilestone: Boolean = false,
    val photos: List<EntryPhoto> = emptyList()
)

data class EntryPhoto(
    val id: Long = 0L,
    val entryId: Long = 0L,
    val fileName: String,       // UUID generado internamente (ej. "a1b2c3d4.jpg")
    val caption: String? = null,
    val orderIndex: Int = 0
)

enum class Mood {
    EUFORICA,
    EMPODERADA,
    TRANQUILA,
    VULNERABLE,
    ANSIOSA
}

enum class Category {
    EXPRESION_ESTILO,
    SOCIAL,
    EMOCIONAL,
    HITO
}
```

---

## 3. Contratos de la Capa de Dominio (Interfaces)

Estas interfaces desacoplan la lógica de negocio de las librerías de Android, permitiendo inyectar *Fakes* o *Mocks* (con MockK) en los tests unitarios:

### 3.1. `JournalRepository`
*   `observeEntries(filter: EntryFilter): Flow<List<JournalEntry>>` — Stream reactivo de entradas ordenadas de más reciente a más antigua.
*   `getEntryById(id: Long): JournalEntry?` — Obtiene el detalle completo de una entrada con sus fotos.
*   `saveEntry(entry: JournalEntry): Result<Long>` — Crea o actualiza una entrada y sus referencias de fotos en una transacción atómica.
*   `deleteEntry(id: Long): Result<Unit>` — Elimina la entrada en DB y dispara la limpieza de sus archivos físicos.

### 3.2. `PhotoStorageManager`
*   `importPhotoToPrivateStorage(sourceUriString: String): Result<String>` — Copia el flujo de bytes desde la galería externa al directorio interno privado (`filesDir/private_photos/`), comprimiendo si excede el tamaño máximo y devolviendo el `fileName` generado.
*   `deletePrivatePhoto(fileName: String): Result<Unit>` — Elimina el archivo físico del almacenamiento interno.
*   `prepareShareableFile(fileName: String): Result<String>` — Copia temporalmente la foto a un directorio de caché controlado para ser expuesta vía `FileProvider`.
*   `clearShareCache(): Unit` — Limpia archivos temporales compartidos.

### 3.3. `SecurityManager`
*   `isPinConfigured(): Boolean` — Verifica si el usuario ya creó un PIN inicial.
*   `setupPin(rawPin: String): Result<Unit>` — Genera un *salt* aleatorio, hashea el PIN (SHA-256 / PBKDF2) y lo guarda de forma cifrada.
*   `validatePin(inputPin: String): Boolean` — Compara el hash del PIN ingresado contra el almacenado.
*   `isBiometricAvailable(): Boolean` — Consulta si el dispositivo soporta autenticación biométrica activa.

---

## 4. Casos de Uso (UseCases)

Cada regla de negocio de la Capa 1 se encapsula en un Caso de Uso con responsabilidad única:

| Caso de Uso | Responsabilidad y Validaciones |
| :--- | :--- |
| `CreateOrUpdateEntryUseCase` | Valida que el contenido o al menos una foto estén presentes, asigna timestamps y persiste vía `JournalRepository`. |
| `DeleteEntryUseCase` | Obtiene las fotos asociadas a la entrada, borra los archivos físicos mediante `PhotoStorageManager` y elimina el registro en DB. |
| `ImportPhotoUseCase` | Valida que no se supere el límite de fotos por entrada (ej. máx. 5 en MVP) y delega el copiado privado. |
| `GetFilteredTimelineUseCase` | Combina criterios de búsqueda (categoría, estado de ánimo, solo hitos, texto libre) sobre el flujo de entradas. |
| `PrepareShareContentUseCase` | Formatea el texto de la entrada y prepara la URI temporal de la foto (si el usuario eligió incluirla) asegurando que no se filtren rutas internas. |
| `VerifyAccessUseCase` | Gestiona el conteo de intentos fallidos de PIN y determina si corresponde aplicar un *cooldown* temporal (ej. 30 segundos tras 5 intentos fallidos). |

---

## 5. Máquina de Estados de Seguridad y Sesión

El ciclo de vida de seguridad se modela mediante una máquina de estados finitos para evitar fugas visuales:

```text
                   ┌─────────────────────┐
                   │   AppInitialized    │
                   └──────────┬──────────┘
                              │
              ┌───────────────┴───────────────┐
              ▼ (No existe PIN)               ▼ (Existe PIN)
   ┌─────────────────────┐         ┌─────────────────────┐
   │   OnboardingPin     │         │       Locked        │◀────────┐
   └──────────┬──────────┘         └──────────┬──────────┘         │
              │ PIN creado                    │ PIN / Biometría OK │
              └───────────────┬───────────────┘                    │
                              ▼                                    │
                   ┌─────────────────────┐                         │
                   │      Unlocked       │─────────────────────────┘
                   └─────────────────────┘   Evento: ON_STOP (Background)
                                             o Botón "Bloqueo Rápido"
```

*   **Regla de transición crítica:** Ante el evento `Lifecycle.Event.ON_STOP` (minimizar la app o apagar la pantalla) o al tocar el botón de **Bloqueo Rápido**, el estado pasa inmediatamente a `Locked` y el `BackStack` sensible se oculta tras la pantalla de bloqueo.
*   **Excepción controlada:** Cuando se abre el *Photo Picker* nativo o el *Share Sheet* del sistema (`Intent.ACTION_SEND`), se activa una bandera temporal `isExternalPickerActive = true` para evitar que la app se bloquee mientras el usuario selecciona su foto o elige por qué app compartir.

---

## 6. Manejo de Errores y Casos Borde

1.  **Falla al importar foto (ej. archivo corrupto o sin espacio):** El `ImportPhotoUseCase` captura la excepción, no deja archivos huérfanos en `private_photos/` y emite un mensaje claro en la UI sin perder el texto que el usuario ya había escrito en el borrador.
2.  **Borrado parcial:** Si al eliminar una entrada falla el borrado de un archivo físico, se registra el archivo en una cola de limpieza (`orphaned_files`) para reintentar en el próximo inicio de la app, garantizando que la entrada desaparezca inmediatamente de la vista del usuario.
3.  **Sin conectividad:** Ningún componente de `Data` ni `Domain` solicita permisos de red (`INTERNET`), garantizando por arquitectura que ningún dato salga del dispositivo sin acción explícita del usuario.