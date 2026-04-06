# Architectural Decisions - Weather Component Optimization

---

## 🇪🇸 Castellano: Resumen de Decisiones Técnicas

### 1. Refactorización de Frontend y Seguridad
* **El Problema:** El código legacy era un coladero. Las API Keys estaban expuestas en el navegador y encima se usaba `context='unsafe'` en HTL… básicamente una invitación a que te cuelen un XSS, que es una vulnerabilidad de seguridad web donde atacantes inyectan scripts maliciosos (generalmente JavaScript) en sitios web legítimos.
* **La Solución:** Nos llevamos toda la lógica al **Backend**. Ahora AEM hace todo el trabajo y el navegador solo recibe el HTML ya cocinado.
* **El Razonamiento:** Moviendo la llamada HTTP al servicio OSGi de Java evitamos problemas de CORS, ocultamos el token de la API para siempre y aprovechamos la velocidad del motor de renderizado de AEM. Quitar el `unsafe` del HTL nos devuelve la protección nativa de Sling contra inyecciones de código.
* **Impacto:** Seguridad a nivel serio. Las credenciales se quedan bien escondidas en el servidor y el frontend deja de ser un punto vulnerable.

---

### 2. Soporte Multi-sitio (CAConfig)
* **El Problema:** Todo estaba hardcodeado. Si querías cambiar una API Key o lanzar en otro país… deploy al canto. Poco práctico.
* **La Solución:** Implementamos **Sling Context-Aware Configurations (CAConfig)**.
* **El Razonamiento:** Podríamos haber usado configuraciones OSGi normales, pero esas son "globales" para todo el servidor. CAConfig es la práctica recomendada para entornos *multi-tenant* porque se ata a la ruta del contenido (ej. `/content/es` vs `/content/us`). Esto da autonomía al equipo de negocio para gestionar credenciales por región sin depender de nosotros los desarrolladores.
* **Impacto:** Dependiendo de la ruta, carga su configuración automáticamente. Escalar ya no requiere tocar código.

---

### 3. Rendimiento: Caché en el Servidor
* **El Problema:** Cada visita hacía una llamada a la API. Resultado: lentitud y riesgo de que te bloqueen por exceso de peticiones.
* **La Solución:** Añadimos una caché en memoria con **`ConcurrentHashMap`**.
* **El Razonamiento:** Meter una librería de caché pesada (como Ehcache o Redis) para un simple componente del clima era matar moscas a cañonazos (overkill). `ConcurrentHashMap` es nativo de Java, súper ligero y, lo más importante, es *thread-safe*, lo que significa que el componente OSGi puede manejar miles de peticiones simultáneas sin que la memoria colapse o se corrompa.
* **Impacto:** Si muchos usuarios consultan la misma ciudad, solo se hace una llamada real. El resto va instantáneo desde memoria.

---

### 4. Resiliencia: Timeouts
* **El Problema:** Llamadas síncronas sin límite. Si la API se quedaba pensando… AEM también.
* **La Solución:** Timeouts de **3000ms** tanto en conexión como en lectura.
* **El Razonamiento:** Sling Models adapta los recursos en el mismo hilo de ejecución (thread) que renderiza la página. Si la API de terceros tarda 10 segundos, el usuario se queda viendo una pantalla en blanco 10 segundos. Cortar por lo sano a los 3 segundos asegura que la experiencia de usuario (UX) no se degrade por culpa de servicios externos.
* **Impacto:** Si en 3 segundos no hay respuesta, se corta, se muestra un “N/A” y la página sigue como si nada.

---

### 5. Blindaje del Dispatcher
* **El Problema:** El `filters.any` no tenía bien configurado el filtro, permitiendo el acceso a todo. Acceso a `/bin/*` y datos internos expuestos.
* **La Solución:** Reglas mucho más estrictas.
* **El Razonamiento:** AEM por defecto permite acceder a representaciones `.json` o `.xml` de cualquier nodo. Si no lo bloqueas forzando solo la extensión `.html` en la carpeta `/content/`, cualquiera podría descargarse la estructura interna de tu CMS.
* **Impacto:** Se bloquea todo lo sensible.

---

### 6. Testing: Aseguramiento de Calidad y Prevención de Regresiones
* **El Problema:** El código original carecía de pruebas automáticas. Esto genera deuda técnica y representa un alto riesgo de regresión: cualquier modificación futura podría romper la funcionalidad del componente sin que el equipo lo detecte antes de llegar a producción.
* **La Solución:** Implementamos pruebas unitarias usando **JUnit 5** como framework base y **Mockito** para la simulación de dependencias externas.
* **El Razonamiento:** El testing en AEM requiere aislar la lógica de negocio de la infraestructura del servidor. Mockito nos permite simular el comportamiento de dependencias críticas (como el `WeatherService`) sin realizar llamadas de red reales durante la fase de *build*. Esto garantiza compilaciones rápidas y deterministas. Diseñamos las pruebas para validar tanto el escenario ideal (respuestas exitosas de la API) como los casos límite (valores nulos o vacíos), garantizando que la aplicación no sufra un `NullPointerException` ante caídas del servicio externo.
* **Impacto:** Mayor estabilidad y mantenibilidad del código. Se valida la lógica Java de forma robusta y automática en cada compilación.

---

### 7. Suposiciones (Assumptions)
* **API externa:** Asumimos que `goweather.xyz` suele estar disponible. Si falla, el componente responde con “N/A” sin romper nada.
* **Configuración CAConfig:** El backend está listo. Se asume que los nodos en `/conf/assessment/...` se crearán en despliegue o por los autores.

---

## 🇺🇸 English: Architectural Summary

### 1. Security & Frontend
* **The Mess:** The legacy code was basically an open door. It exposed API Keys directly in the browser and used `context='unsafe'` in HTL—a perfect setup for XSS attacks (a web security vulnerability where attackers inject malicious scripts into trusted websites).
* **The Fix:** We moved everything to the **Server-Side**. The browser now only gets fully rendered HTML.
* **The Reasoning:** Moving the HTTP call to a Java OSGi service eliminates CORS issues, hides API tokens forever, and leverages AEM's fast server-side rendering. Removing `unsafe` restores Sling's native protection against code injections.
* **The Impact:** Serious security upgrade. Credentials stay well hidden on the server, and the frontend is no longer a vulnerable entry point.

---

### 2. Multi-Tenancy (CAConfig)
* **The Mess:** Everything was hardcoded. Want to change an API Key or launch in another country? You needed a full code deploy. Not practical.
* **The Fix:** Implemented **Sling Context-Aware Configurations (CAConfig)**.
* **The Reasoning:** We could have used standard OSGi configs, but those are "global" across the server. CAConfig is the recommended practice for *multi-tenant* environments because it binds the configuration to the content path (e.g., `/content/es` vs `/content/us`). This empowers the business team to manage regional credentials without depending on developers.
* **The Impact:** Depending on the path, it automatically loads its configuration. Scaling no longer requires touching code.

---

### 3. Performance: Server-Side Caching
* **The Mess:** Every visit triggered an API call. Result: slow load times and a high risk of getting blocked for rate limiting.
* **The Fix:** Added in-memory caching with **`ConcurrentHashMap`**.
* **The Reasoning:** Throwing a heavy caching library (like Ehcache or Redis) at a simple weather component would be overkill. `ConcurrentHashMap` is native to Java, super lightweight, and most importantly, it's *thread-safe*, meaning the OSGi component can handle thousands of concurrent requests without memory corruption.
* **The Impact:** If many users check the same city, only one real call is made. The rest is served instantly from memory.

---

### 4. Resilience: Connection Timeouts
* **The Mess:** Unlimited synchronous calls. If the API got stuck thinking... AEM did too.
* **The Fix:** Set timeouts of **3000ms** for both connection and reading.
* **The Reasoning:** Sling Models adapt resources on the same thread that renders the page. If the third-party API takes 10 seconds, the user stares at a blank screen for 10 seconds. Cutting it off at 3 seconds ensures the User Experience (UX) doesn't degrade because of external services.
* **The Impact:** If there's no response in 3 seconds, it cuts the connection, shows "N/A", and the page continues loading as if nothing happened.

---

### 5. Dispatcher Hardening
* **The Mess:** The `filters.any` file wasn't configured properly, allowing access to everything. Access to `/bin/*` and internal data was exposed.
* **The Fix:** Much stricter rules.
* **The Reasoning:** AEM natively allows accessing `.json` or `.xml` representations of any node. If you don't block this by forcing only the `.html` extension in the `/content/` folder, anyone could download the internal structure of your CMS.
* **The Impact:** All sensitive paths are completely blocked.

---

### 6. Automated Testing: Quality Assurance and Regression Prevention
* **The Mess:** The original code lacked automated tests. This creates technical debt and introduces a high risk of regression: any future modification could break the component's functionality without the team noticing before it hits production.
* **The Fix:** We implemented unit tests using **JUnit 5** as the base framework and **Mockito** for external dependency simulation.
* **The Reasoning:** Testing in AEM requires isolating business logic from the server infrastructure. Mockito allows us to mock the behavior of critical dependencies (like `WeatherService`) without making real network calls during the build phase. This ensures fast and deterministic builds. The tests are designed to validate both the happy path (successful API responses) and edge cases (null or empty values), ensuring the application degrades gracefully and avoids throwing `NullPointerExceptions` when external services fail.
* **The Impact:** Increased code stability and maintainability. The core Java logic is robustly and automatically validated on every build.

---

### 7. Assumptions
* **Third-Party API:** We assume `goweather.xyz` is usually available. If it fails, the component responds with "N/A" without breaking anything.
* **CAConfig Provisioning:** The backend is ready. It is assumed that the actual config nodes under `/conf/assessment/...` will be created during deployment or by the authors.