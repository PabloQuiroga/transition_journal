# Capa 1: Especificación Funcional y Reglas de Negocio
**Proyecto:** Diario Íntimo de Transición y Expresión (Nombre clave: *Refugio*)  
**Versión:** 1.0  
**Estado:** Borrador para validación  

---

## 1. Propósito y Alcance del Producto

### 1.1 Visión
Proveer una aplicación móvil personal, segura y completamente *offline* para acompañar el proceso de transición social, explorar la expresión de género y llevar un registro emocional cotidiano.

### 1.2 Alcance del MVP (In-Scope)
*   **Bóveda de Privacidad:** Acceso protegido mediante PIN numérico y biometría local, con bloqueo automático y protección de pantalla.
*   **Diario Multimodal:** Creación, edición y eliminación de entradas con estado emocional, categoría, texto libre y fotografías privadas.
*   **Línea de Tiempo e Hitos:** Visualización cronológica con filtros por categoría/emoción y una vista dedicada a hitos destacados.
*   **Compartir Controlado y Respaldo:** Exportación puntual de una entrada (texto y/o fotos) mediante el menú nativo de compartir, y respaldo completo de datos en archivo local.

### 1.3 Fuera de Alcance (Out-of-Scope en MVP)
*   Sincronización en la nube, cuentas de usuario o servidores remotos.
*   Funciones sociales públicas, feed comunitario o mensajería integrada.
*   Seguimiento médico/hormonal complejo (se mantiene enfocado en lo social, expresivo y emocional).

---

## 2. Reglas de Negocio y Privacidad (Invariantes)

1. **RN-01 (Cero Red):** La aplicación nunca debe realizar conexiones salientes ni solicitar permisos de internet. Todo procesamiento ocurre localmente en el dispositivo.
2. **RN-02 (Aislamiento de Medios):** Toda fotografía incorporada al diario debe copiarse a un directorio privado exclusivo de la aplicación y eliminarse sus metadatos EXIF de ubicación. Nunca debe indexarse en la galería pública del teléfono.
3. **RN-03 (Borrado Efectivo):** Al eliminar una foto de una entrada o eliminar una entrada completa, los archivos físicos asociados deben borrarse inmediatamente del almacenamiento interno sin dejar residuos.
4. **RN-04 (Compartir Efímero):** Al compartir una entrada hacia otra aplicación externa, los archivos temporales generados para dicha acción deben residir en caché temporal y purgarse tras finalizar la acción.
5. **RN-05 (No Recuperación Remota):** Al no existir servidores ni cuentas vinculadas, si el usuario olvida su PIN y no tiene biometría activa, no existe mecanismo de recuperación por correo; la única salida es un restablecimiento total (borrado de datos locales).

---

## 3. Historias de Usuario y Criterios de Aceptación

### Módulo 1: Seguridad y Control de Acceso

#### US-01: Configuración Inicial de PIN y Biometría
**Como** usuaria que abre la app por primera vez,  
**quiero** establecer un PIN numérico y habilitar opcionalmente mi huella/rostro,  
**para** asegurar que solo yo pueda acceder a mi diario.

*   **AC-01.1 (Creación exitosa):**
    *   **Dado** que no existe un PIN configurado en el dispositivo,
    *   **Cuando** ingreso un PIN válido (4 a 6 dígitos) y lo confirmo exactamente igual en el segundo paso,
    *   **Entonces** el PIN se guarda de forma cifrada y accedo a la pantalla principal del diario.
*   **AC-01.2 (PIN no coincidente):**
    *   **Dado** que estoy configurando mi PIN por primera vez,
    *   **Cuando** la confirmación no coincide con el primer PIN ingresado,
    *   **Entonces** se muestra un mensaje de error claro y se reinicia el paso de confirmación sin guardar nada.
*   **AC-01.3 (Activación biométrica):**
    *   **Dado** que el dispositivo cuenta con hardware biométrico configurado,
    *   **Cuando** termino de crear mi PIN,
    *   **Entonces** se me ofrece activar el desbloqueo biométrico en un solo toque.

#### US-02: Autenticación y Bloqueo Automático
**Como** usuaria habitual,  
**quiero** que la app solicite autenticación al abrirse o tras pasar a segundo plano,  
**para** evitar que alguien vea mi contenido si dejo el teléfono desbloqueado.

*   **AC-02.1 (Desbloqueo exitoso):**
    *   **Dado** que la aplicación está bloqueada,
    *   **Cuando** ingreso el PIN correcto o valido mi biometría,
    *   **Entonces** se desbloquea la sesión y visualizo mi línea de tiempo.
*   **AC-02.2 (Bloqueo por segundo plano):**
    *   **Dado** que la aplicación está desbloqueada,
    *   **Cuando** minimizo la app y transcurre el tiempo de gracia configurado (por defecto: inmediato),
    *   **Entonces** al volver a abrirla se muestra nuevamente la pantalla de bloqueo.
*   **AC-02.3 (Bloqueo rápido manual):**
    *   **Dado** que estoy navegando en cualquier pantalla del diario,
    *   **Cuando** presiono el botón de "Bloqueo rápido" en la barra superior,
    *   **Entonces** la sesión se cierra instantáneamente y se muestra la pantalla de bloqueo.

---

### Módulo 2: Registro y Gestión de Entradas

#### US-03: Crear una Entrada del Diario
**Como** usuaria,  
**quiero** registrar mis emociones, pensamientos y vivencias del día,  
**para** documentar mi proceso de transición social y expresión.

*   **AC-03.1 (Guardado válido):**
    *   **Dado** que estoy en el editor de nueva entrada,
    *   **Cuando** selecciono un estado emocional, una categoría, escribo un texto (o adjunto al menos una foto) y presiono "Guardar",
    *   **Entonces** la entrada se persiste con la fecha/hora actual y aparece en la parte superior de la línea de tiempo.
*   **AC-03.2 (Validación mínima):**
    *   **Dado** que estoy en el editor de nueva entrada,
    *   **Cuando** intento guardar sin haber seleccionado un estado emocional o sin haber ingresado ni texto ni fotos,
    *   **Entonces** el botón de guardar permanece deshabilitado (o indica el campo requerido).
*   **AC-03.3 (Título automático):**
    *   **Dado** que dejo el campo de título vacío al guardar una entrada válida,
    *   **Cuando** se guarda la entrada,
    *   **Entonces** el sistema asigna automáticamente un título descriptivo basado en la fecha (ej. *"Domingo, 15 de noviembre"*).

#### US-04: Adjuntar y Gestionar Fotografías Privadas
**Como** usuaria,  
**quiero** agregar fotos de mis looks, maquillaje o momentos significativos a una entrada,  
**para** observar mi evolución visual en un entorno seguro.

*   **AC-04.1 (Importación aislada):**
    *   **Dado** que selecciono una o varias fotos desde el selector del sistema,
    *   **Cuando** se adjuntan a la entrada,
    *   **Entonces** se copian al almacenamiento interno privado de la app y se muestran en el carrusel de la entrada.
*   **AC-04.2 (Pie de foto y eliminación):**
    *   **Dado** que una entrada tiene fotos adjuntas,
    *   **Cuando** agrego una nota breve a una foto o decido quitarla y guardar,
    *   **Entonces** se actualiza el pie de foto o se elimina el archivo físico de la foto quitada.

#### US-05: Editar y Eliminar Entradas
**Como** usuaria,  
**quiero** modificar una entrada existente o eliminarla definitivamente,  
**para** corregir errores o depurar registros que ya no deseo conservar.

*   **AC-05.1 (Edición):**
    *   **Dado** que abro el detalle de una entrada existente,
    *   **Cuando** edito su texto, categoría, emoción, estado de hito o fotos y guardo,
    *   **Entonces** los cambios se reflejan manteniendo la fecha de creación original y registrando la fecha de modificación.
*   **AC-05.2 (Eliminación con confirmación):**
    *   **Dado** que solicito eliminar una entrada,
    *   **Cuando** confirmo la acción en el diálogo de advertencia,
    *   **Entonces** la entrada y todas sus fotos asociadas se eliminan permanentemente del almacenamiento.

---

### Módulo 3: Exploración y Seguimiento Emocional

#### US-06: Línea de Tiempo, Búsqueda y Filtros
**Como** usuaria,  
**quiero** recorrer mis entradas pasadas y filtrarlas por emoción, categoría o palabras clave,  
**para** encontrar recuerdos o analizar cómo me fui sintiendo.

*   **AC-06.1 (Filtrado combinado):**
    *   **Dado** que tengo múltiples entradas registradas,
    *   **Cuando** selecciono un filtro de categoría (ej. *"Expresión y Estilo"*) o un estado emocional,
    *   **Entonces** la lista muestra únicamente las entradas que coinciden con los criterios activos.
*   **AC-06.2 (Vista de Hitos):**
    *   **Dado** que marqué ciertas entradas como *"Hito Destacado"*,
    *   **Cuando** activo el filtro o pestaña de *"Hitos"*,
    *   **Entonces** visualizo únicamente esos momentos clave ordenados cronológicamente.

---

### Módulo 4: Compartir Controlado y Respaldo Local

#### US-07: Compartir una Entrada Puntual
**Como** usuaria,  
**quiero** compartir una entrada específica con alguien de confianza o mi terapeuta,  
**para** mostrarle un avance o compartir cómo me siento sin exponer el resto de mi diario.

*   **AC-07.1 (Selección granular):**
    *   **Dado** que presiono "Compartir" en el detalle de una entrada,
    *   **Cuando** elijo qué elementos incluir (ej. solo texto, texto + foto seleccionada),
    *   **Entonces** se abre el selector nativo de Android con el contenido elegido listo para enviar por la app que seleccione (ej. WhatsApp, Telegram).

#### US-08: Exportación e Importación de Respaldo Local
**Como** usuaria,  
**quiero** exportar todo mi diario en un archivo local y poder restaurarlo si cambio de dispositivo,  
**para** proteger mis recuerdos ante pérdida o cambio de teléfono.

*   **AC-08.1 (Exportación completa):**
    *   **Dado** que inicio una exportación desde Ajustes,
    *   **Cuando** selecciono la ubicación de destino en el almacenamiento del dispositivo,
    *   **Entonces** se genera un archivo empaquetado (`.zip`) con todas las entradas y fotografías.
*   **AC-08.2 (Restauración):**
    *   **Dado** que selecciono un archivo de respaldo válido,
    *   **Cuando** confirmo la importación,
    *   **Entonces** se restauran las entradas y fotos sin duplicar registros existentes.

---

## 4. Catálogos de Dominio (Valores Iniciales)

### 4.1 Estados Emocionales (`Mood`)
| Clave | Etiqueta UI | Descripción / Matiz |
| :--- | :--- | :--- |
| `EUPHORIC` | Eufórica | Alegría intensa, validación de género, plenitud |
| `EMPOWERED` | Empoderada | Seguridad, orgullo, firmeza en la propia identidad |
| `CALM` | Tranquila | Paz, estabilidad, comodidad cotidiana |
| `VULNERABLE` | Vulnerable | Sensibilidad, nostalgia, necesidad de contención |
| `ANXIOUS` | Ansiosa | Nerviosismo, miedo social, incertidumbre |

### 4.2 Categorías de Experiencia (`Category`)
| Clave | Etiqueta UI | Ejemplos de uso |
| :--- | :--- | :--- |
| `EXPRESSION` | Expresión y Estilo | Pruebas de ropa, maquillaje, cuidado personal, cabello |
| `SOCIAL` | Transición Social | Salidas, uso de pronombres/nombre, charlas con entorno |
| `EMOTIONAL` | Reflexión Emocional | Pensamientos internos, sesiones de terapia, autocuidado |
| `MILESTONE` | Hito / Primera Vez | Primeras salidas, compras significativas, logros personales |
```

Revisá este primer documento (`01-especificacion.md`). Si te parece bien o querés ajustar alguna historia de usuario, regla de negocio o catálogo de emociones/categorías, avisame; si ya te cierra así, avanzamos con la **Capa 2: Diseño Lógico y Arquitectura**.