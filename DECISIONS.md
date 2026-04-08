# Architectural Decisions - Weather Component Optimization

---

## 🇪🇸 Castellano: Resumen de Decisiones Técnicas

### 1. Refactorización de Frontend y Seguridad
* **El Problema:** El código legacy exponía API Keys en el navegador y usaba `context='unsafe'` en HTL, facilitando ataques XSS.
* **La Solución:** Toda la lógica se movió al **Backend**. El navegador solo recibe HTML renderizado.
* **El Razonamiento:** Ocultamos tokens, evitamos problemas de CORS y recuperamos la protección nativa de Sling contra inyecciones de código.

### 2. Soporte Multi-sitio (CAConfig)
* **El Problema:** Configuración hardcodeada y global.
* **La Solución:** Implementación de **Sling Context-Aware Configurations (CAConfig)**.
* **El Razonamiento:** Permite gestionar credenciales de forma independiente por ruta (ej. `/content/us` vs `/content/es`), facilitando la autonomía regional.

### 3. Rendimiento: Caché en el Servidor
* **El Problema:** Exceso de llamadas síncronas a la API externa.
* **La Solución:** Caché en memoria con **`ConcurrentHashMap`**.
* **El Razonamiento:** Solución ligera y *thread-safe* para evitar el colapso de memoria y mejorar la velocidad de respuesta.

### 4. Resiliencia: Timeouts
* **El Problema:** Riesgo de bloqueo del hilo de renderizado por APIs lentas.
* **La Solución:** Timeouts de **3000ms**.
* **El Razonamiento:** Garantiza que la experiencia de usuario (UX) no se degrade si el servicio externo falla.

### 5. Blindaje del Dispatcher
* **El Problema:** Filtros demasiado permisivos.
* **La Solución:** Reglas estrictas en `filters.any`.
* **El Razonamiento:** Bloqueo de acceso a nodos internos (.json/.xml) y protección de la carpeta `/bin`.

### 6. Testing: Calidad y Regresiones
* **La Solución:** Pruebas unitarias con **JUnit 5** y **Mockito**.
* **El Razonamiento:** Validación automática de la lógica de negocio y manejo de errores (edge cases) sin depender de la red.

### 7. Suposiciones (Assumptions)
* Se asume la disponibilidad general de la API externa. Si falla, el componente muestra "N/A" con elegancia.

### 8. Corrección del Pipeline de Build (Maven)
* **La Solución:** Refactorización de los archivos `pom.xml` en `all`, `ui.apps` y `ui.config`.
* **El Razonamiento:** Se corrigió el embebido del bundle OSGi y el orden de dependencias para asegurar que el código Java llegue correctamente al servidor.

### 9. Nota sobre el Entorno Local (Responsive Grid)
* **Estado:** Durante la validación en el SDK local, se identificó un problema persistente en la visualización del `responsivegrid` en el editor.
* **Acción realizada:** Aunque el error visual persistió en el entorno de desarrollo, se realizó toda la **refactorización del código**, la reparación de las **políticas de diseño (Policies)** y la reestructuración de los nodos de la **plantilla (Template)** en el repositorio. La lógica está implementada siguiendo los estándares de AEM as a Cloud Service.

---

## 🇺🇸 English: Architectural Summary

### 1. Security & Frontend
* **The Mess:** Legacy code exposed API Keys and used `context='unsafe'`.
* **The Fix:** Moved logic to **Server-Side**. The browser only receives rendered HTML.
* **The Reasoning:** Hidden tokens, no CORS issues, and restored Sling native XSS protection.

### 2. Multi-Tenancy (CAConfig)
* **The Mess:** Hardcoded global configurations.
* **The Fix:** Implemented **Sling Context-Aware Configurations (CAConfig)**.
* **The Reasoning:** Enables independent credential management per site path (e.g., `/content/us` vs `/content/es`).

### 3. Performance: Server-Side Caching
* **The Mess:** Excessive synchronous API calls.
* **The Fix:** In-memory caching with **`ConcurrentHashMap`**.
* **The Reasoning:** Lightweight and thread-safe solution to prevent memory issues and improve speed.

### 4. Resilience: Connection Timeouts
* **The Mess:** Slow APIs could block the rendering thread.
* **The Fix:** **3000ms** timeouts.
* **The Reasoning:** Ensures UX doesn't degrade due to third-party service delays.

### 5. Dispatcher Hardening
* **The Mess:** Overly permissive filters.
* **The Fix:** Stricter rules in `filters.any`.
* **The Reasoning:** Blocks access to internal nodes and protects sensitive paths.

### 6. Automated Testing
* **The Fix:** Unit tests using **JUnit 5** and **Mockito**.
* **The Reasoning:** Automatic validation of business logic and edge-case handling without network dependency.

### 7. Assumptions
* Assumes external API availability. If it fails, the component displays "N/A" gracefully.

### 8. Build Pipeline Fixes (Maven)
* **The Fix:** Refactored `pom.xml` files in `all`, `ui.apps`, and `ui.config`.
* **The Reasoning:** Fixed OSGi bundle embedding and dependency order to ensure Java code reaches the server.

### 9. Note on Local Environment (Responsive Grid)
* **Status:** During validation on the local SDK, a persistent issue was identified regarding the `responsivegrid` visibility within the editor.
* **Action taken:** Although the visual glitch persisted in the local dev environment, a full **code refactor**, **Policy** repair, and **Template** node restructuring were completed in the repository. The logic is fully implemented according to AEM as a Cloud Service standards.