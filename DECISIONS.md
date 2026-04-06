# Architectural Decisions - Weather Component Optimization

This document outlines the strategic refactoring choices made to transform the legacy Weather implementation into a secure, high-performance, and scalable AEM as a Cloud Service component.

---

## 🇪🇸 Castellano: Resumen de Decisiones Técnicas

### 1. Refactorización de Frontend y Seguridad: El "Búnker"
* **El Problema:** El código legacy exponía las API Keys en el navegador (Client-side fetch) y usaba `context='unsafe'` en HTL, facilitando ataques XSS y robo de credenciales.
* **La Solución:** Movimos toda la lógica al **Backend**. El navegador ahora solo recibe el HTML procesado; no sabe que existe una API externa.
* **Impacto:** Seguridad nivel enterprise. Las "llaves" del servicio están protegidas en el servidor y el HTML sigue los estándares de seguridad de AEM.

### 2. Soporte Multi-sitio (CAConfig): Pensando en Global
* **El Problema:** Configuraciones "hardcoded" que obligaban a tocar código para cambiar una API Key o añadir un nuevo país.
* **La Solución:** Implementación de **Sling Context-Aware Configurations (CAConfig)**.
* **Impacto:** El componente ahora es "consciente" de su ubicación. Si se usa en `/content/us` usa la config de USA; en `/content/es` usa la de España. Cero cambios de código para escalar a nuevos mercados.

### 3. Rendimiento: Caché en el Servidor (Eficiencia)
* **El Problema:** Cada carga de página disparaba una petición a internet. Lento para el usuario y riesgo de bloqueos por parte del proveedor de clima (Rate Limiting).
* **La Solución:** Caché en el servicio OSGi mediante un **ConcurrentHashMap** thread-safe.
* **Impacto:** Si 100 personas consultan la misma ciudad, solo se hace 1 llamada a la API. Las otras 99 reciben una respuesta instantánea desde la memoria del servidor.

### 4. Resiliencia: Tiempos de Espera (Timeouts)
* **El Problema:** Llamadas síncronas que podían dejar colgado el renderizado de la página si la API externa iba lenta o fallaba.
* **La Solución:** Configuración de **Timeouts de 3000ms** (conexión y lectura).
* **Impacto:** Si la API no responde en 3 segundos, el componente falla de forma controlada (fail-fast), permitiendo que el resto de la página cargue perfectamente.

### 5. Blindaje del Dispatcher: El Portero
* **El Problema:** Filtros permisivos que dejaban rutas internas (`/bin/*`) y datos del JCR expuestos.
* **La Solución:** Hardening de `filters.any`.
* **Impacto:** Se bloqueó el acceso a servlets internos y se restringió el acceso a `/content` solo para extensiones `.html`, evitando la extracción de datos en JSON/XML.

---

## 🇺🇸 English: Architectural Summary

### 1. Security & Frontend: Moving to the Bunker
* **Challenge:** Legacy code exposed API Keys via client-side fetch and used `context='unsafe'`, risking XSS and credential theft.
* **Solution:** Moved all integration logic to the **Server-Side**. The browser now only receives "ready-to-render" HTML.
* **Impact:** Enterprise-grade security. API credentials stay hidden on the server, and HTL follows AEM security best practices.

### 2. Multi-Tenancy (CAConfig): Scaling Without Pain
* **Challenge:** Hardcoded configurations required code deployments for every minor change or new market launch.
* **Solution:** Implemented **Sling Context-Aware Configurations (CAConfig)**.
* **Impact:** The component is now "path-aware." It automatically fetches the correct config based on its location (e.g., `/content/us` vs `/content/es`). Zero-code scaling for international rollouts.

### 3. Performance: Server-Side Caching (API Friendliness)
* **Challenge:** Every page load triggered a network call, causing slow load times and risking API rate-limiting blocks.
* **Solution:** Implemented a caching layer using a thread-safe **ConcurrentHashMap** within the OSGi service.
* **Impact:** Drastic reduction in network overhead. Subsequent requests for the same city are served near-instantly from memory.

### 4. Resilience: Connection Timeouts
* **Challenge:** Synchronous calls without limits could hang the AEM rendering thread if the third-party service was slow.
* **Solution:** Explicitly set **3000ms Connection and Read Timeouts**.
* **Impact:** Fail-fast mechanism. If the external API hangs, the component fails gracefully without impacting the overall page performance.

### 5. Dispatcher Hardening: The Strict Bouncer
* **Challenge:** Permissive filters allowed access to internal servlets and sensitive JCR structures.
* **Solution:** Hardened `filters.any` rules.
* **Impact:** Restricted `/bin/*` access to protect internal servlets and enforced `.html` extensions on `/content` to prevent unauthorized JCR data sniffing (XML/JSON).