# Matriz de Casos Borde y Pruebas Unitarias (TDD)
**Proyecto:** Diario Íntimo de Transición y Expresión (Nombre clave: *Refugio*)  
**Estado:** Documento Vivo para Seguimiento TDD  

---

## 1. Capa de Dominio (`domain`)

| Componente | Caso Borde Identificado | Criterio de Aceptación / Regla | Estado |
| :--- | :--- | :--- | :--- |
| `SetupPinUseCase` | PIN con letras o caracteres alfanuméricos (ej. `"123a"`) | `AC-01.1`: Solo dígitos numéricos | Completado (Green) |
| `SetupPinUseCase` | PIN mayor a 6 dígitos (ej. `"1234567"`) | `AC-01.1`: Máximo 6 dígitos | Completado (Green) |
| `CreateOrUpdateEntryUseCase` | Entrada sin texto pero con al menos 1 foto adjunta | `AC-03.1`: Válida si contiene foto o texto | Completado (Green) |
| `CreateOrUpdateEntryUseCase` | Entrada con título explícito previo | `AC-03.3`: Conservar título del usuario | Completado (Green) |
| `CreateOrUpdateEntryUseCase` | Conservar `createdAt` y actualizar `updatedAt` en edición | `AC-05.1`: Preservar fecha original | Completado (Green) |
| `VerifyAccessUseCase` | Reseteo del contador de fallos tras ingresar PIN correcto | `AC-02.1`: PIN correcto limpia intentos | Completado (Green) |
| `VerifyAccessUseCase` | Intento de PIN mientras el Cooldown de 30s está activo | `AC-02.2`: Bloqueo estricto sin reevaluar | Pendiente |
| `PrepareShareContentUseCase` | Foto seleccionada no existe físicamente en el storage | `RN-04`: Retornar Result de fallo controlado | Pendiente |
| `ImportPhotoUseCase` | Importar más de 5 fotos en una misma entrada | `US-04`: Límite máximo de 5 fotos | Completado (Green) |

---

## 2. Capa de Datos e Infraestructura (`data` / `core`)

| Componente | Caso Borde Identificado | Criterio de Aceptación / Regla | Estado |
| :--- | :--- | :--- | :--- |
| `JournalDao` | Inserción de entrada con ID duplicado (conflicto) | `REPLACE` OnConflictStrategy | Completado (Green) |
| `PhotoEntity` | Borrado de entrada con fotos asociadas en SQLite | `ForeignKey.CASCADE` borra fotos | Completado (Green) |
| `PrivatePhotoStorageManager` | Intento de borrar foto inexistente en disco | `Result.success` idempotente | Completado (Green) |

---

## 3. Capa de Presentación (`ui`)

| Componente | Caso Borde Identificado | Criterio de Aceptación / Regla | Estado |
| :--- | :--- | :--- | :--- |
| `LockViewModel` | `verifyPin` falla tras 5 intentos | Transición a `Locked` con cooldown | Completado (Green) |
| `LockViewModel` | Minimizar app (`ON_STOP`) o botón de bloqueo rápido | Transición inmediata a `Locked` | Pendiente |
| `EntryDetailViewModel` | Cargar detalle de entrada con ID no existente en DB | Mostrar mensaje "Entrada no encontrada" | Completado (Green) |
| `TimelineViewModel` | Filtrado combinado (Categoría + Hito + Query de texto) | `AC-06.1`: Coincidencia multi-criterio | Completado (Green) |
