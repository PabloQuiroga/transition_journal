# ADR 001: Evolución del Modelo de Dominio de JournalEntry

**Estado:** Aprobado  
**Fecha:** 2026-11-17  
**Autor:** Senior Kotlin/Android Engineer  

## 1. Contexto y Problema

La entidad `JournalEntry` representaba originalmente la fecha de registro y la fecha del acontecimiento bajo una única propiedad `createdAt`. Esto impedía registrar vivencias pasadas retroactivas sin alterar la auditoría de creación. Además, la propiedad `mood` era obligatoria, lo que forzaba una clasificación emocional en entradas informativas o estéticas, y `EntryPhoto` contenía rutas de almacenamiento local (`absolutePath`) acopladas a la infraestructura.

## 2. Decisión Arquitectónica

Se aprueba evolucionar el modelo de dominio de la siguiente manera:

1. **Fecha del Acontecimiento (`occurredAt`):** Incorporar `occurredAt: Instant = createdAt` en `JournalEntry` para separar la auditoría del sistema (`createdAt`) de la fecha real de la vivencia (`occurredAt`). La línea de tiempo se ordenará por `occurredAt DESC`. En la UI se etiquetará simplemente como **"Fecha"**.
2. **Estado de Ánimo Opcional (`mood: Mood?`):** Modificar `mood` para admitir valores nulos (`null`), permitiendo registrar entradas sin clasificación emocional forzada.
3. **Desacoplamiento en `EntryPhoto`:** Eliminar `absolutePath` del modelo de dominio `EntryPhoto`, manteniendo únicamente la referencia de identidad unívoca (`fileName`) y metadatos (`caption`, `orderIndex`).
4. **Extensibilidad sin Entidades Prematuras:** Mantener `JournalEntry` enfocado en su rol narrativo. No crear entidades prematuras de entrenamiento o vestimenta sin historias de usuario asociadas (respetando YAGNI y DDD).

## 3. Consecuencias

- **Persistencia (Room DB):** Se requiere incrementar `version = 2` en `RefugioDatabase` y definir `MIGRATION_1_2` agregando `occurredAt` a la tabla `journal_entries` y permitiendo `NULL` en `mood`.
- **Lógica de Consulta (DAO):** La consulta `observeAllEntries` en `JournalDao` pasa a ordenar por `occurredAt DESC`.
- **Casos de Uso y UI:** Se actualizan UseCases y ViewModels para manejar `occurredAt` y `mood` opcional.
