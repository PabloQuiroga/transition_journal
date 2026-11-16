# Directivas para Agentes de IA en transition_journal

## 1. Jerarquía y Fuentes de Verdad

1. **Metodología de Trabajo:** `master_prompt.md` establece la metodología general.
2. **Directivas Operativas:** Este archivo (`AGENTS.md`) define el comportamiento dentro del repositorio.
3. **Visión del Producto:** `README.md` define el marco conceptual global.
4. **Requisitos Funcionales:** `docs/01-especificacion.md` es la fuente de verdad del negocio y reglas funcionales.
5. **Diseño Lógico:** `docs/02-diseno-logico.md` es la fuente de verdad de arquitectura y modelos de dominio.
6. **Implementación:** `docs/03-implementacion-y-ci.md` es la fuente de verdad técnica de paquetes, dependencias y testing.

En caso de contradicción entre fuentes, el agente debe detenerse, reportar el conflicto concreto y solicitar la resolución explícita del usuario.

## 2. Restricciones Clave del Proyecto

- **Privacidad Strict Offline-First:** Está estrictamente prohibido agregar la declaración de permiso `<uses-permission android:name="android.permission.INTERNET"/>` o bibliotecas con llamadas a red remota.
- **Aislamiento de Archivos:** Las fotos e información sensible deben ser almacenadas únicamente en el almacenamiento interno privado (`filesDir`).
- **Spec-Driven Development:** Ningún componente técnico o pantalla se implementará sin estar previamente respaldado por su especificación y diseño en la carpeta `docs/`.
- **Modo de Operación Incremental:** Proponer un solo paso concreto a la vez, explicando el por qué y esperando la confirmación del usuario antes de modificar archivos o escribir código.
