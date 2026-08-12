# Migración a Angular — Sistema de Matrículas Lapreplus

Este directorio (`migracion-angular/`) es una **copia de trabajo**. Tu proyecto original
en `C:\Programing\JAVA\Academia` no se ha modificado.

```
migracion-angular/
├── backend/     ← copia de tu Spring Boot, con la API REST + JWT añadidos
└── frontend/    ← nuevo proyecto Angular (SPA)
```

La estrategia es **incremental**: el backend sigue sirviendo la app Thymeleaf actual y,
en paralelo, expone una API REST bajo `/api/**` protegida con JWT. Angular consume esa API.
Migramos **módulo por módulo**; el primero (Cursos) queda como plantilla de referencia.

---

## 1. Levantar el backend (API REST)

Requisitos: JDK 21 y MySQL corriendo (misma base `Academia` de siempre).

```bash
cd backend
./mvnw clean spring-boot:run       # Windows: mvnw.cmd clean spring-boot:run
```

El backend arranca en `http://localhost:9090`. Novedades añadidas en esta copia:

| Endpoint | Descripción |
|---|---|
| `POST /api/auth/login` | Devuelve un JWT. Body: `{ "username": "...", "password": "..." }` |
| `GET  /api/auth/me` | Datos del usuario del token actual |
| `GET  /api/cursos` | Listado paginado (`?q=&nivel=&area=&page=&size=`) |
| `GET  /api/cursos/{id}` | Un curso |
| `POST /api/cursos` | Crear (rol ADMIN) |
| `PUT  /api/cursos/{id}` | Actualizar (rol ADMIN) |
| `DELETE /api/cursos/{id}` | Eliminar (rol ADMIN) |
| `GET  /api/cursos/niveles` · `/api/cursos/areas?nivel=` | Catálogos para selectores |

Usuarios de prueba sembrados por `DataInitializer`: `admin/admin123`, `cajero/cajero123`, `auxiliar/auxiliar123`.

Prueba rápida del login:
```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

### Qué se agregó y por qué no rompe nada
- `pom.xml`: dependencias `jjwt` (generación/validación de tokens).
- `application.yml`: bloque `app.jwt.*` y `app.cors.*`.
- `security/JwtService`, `security/JwtAuthenticationFilter`: emisión y validación de JWT.
- `config/ApiSecurityConfig` (`@Order(1)`, `securityMatcher("/api/**")`, **stateless**): cadena de
  seguridad SOLO para la API. La cadena existente (`SecurityConfig`) recibió `@Order(2)` y sigue
  gobernando toda la parte Thymeleaf con login por formulario. Las dos conviven.
- `controller/api/*`, `dto/api/*`: la nueva capa REST. **Reutiliza los mismos `Service`**, así que
  la lógica de negocio no se duplica.

---

## 2. Levantar el frontend (Angular)

Requisitos: Node.js 20.19+ o 22+.

```bash
cd frontend
npm install
npm start          # abre http://localhost:4200
```

Inicia sesión con `admin/admin123`. Verás el módulo **Cursos** funcionando contra la API.

---

## 3. Cómo migrar el siguiente módulo (patrón a repetir)

Cada módulo se migra copiando el patrón de **Cursos**. Para el módulo `X`:

**Backend** (en `backend/`)
1. `dto/api/XDTO.java` — el JSON que devuelve la API (construido dentro del request).
2. `controller/api/XApiController.java` — `@RestController` bajo `/api/x`, reutilizando el `XService` existente.
3. En `ApiSecurityConfig`, añade las reglas de rol para `POST/PUT/DELETE /api/x/**`
   (copiando el criterio de `SecurityConfig` para ese módulo).

**Frontend** (en `frontend/src/app/`)
4. `core/models/x.model.ts` — interfaces `X`, `XForm`, `PaginaX`.
5. `core/services/x.service.ts` — métodos `listar/obtener/crear/actualizar/eliminar`.
6. `features/x/x-lista.component.ts` y `features/x/x-form.component.ts`.
7. Registra las rutas en `app.routes.ts` y el ítem de menú en `shared/layout/main-layout.component.ts`.

### Orden de migración planificado
- **Fase 1 – Catálogo:** Cursos ✅ · Profesores · Ciclos · Sedes · Áreas/Niveles
- **Fase 2 – Núcleo académico:** Alumnos · Matrículas · Pagos
- **Fase 3 – Operación:** Asistencia · Horarios · Horas docentes
- **Fase 4 – Reportes y Dashboard:** Resumen · Reportes (los PDF se siguen generando en el
  backend con flying-saucer y se descargan como blob desde Angular)
- **Fase 5 – Contenido y web pública:** Testimonios · FAQ · Galería · Eventos/Calendario ·
  Pasos de admisión · Logros de ingreso · Solicitudes de información
- **Fase 6 – Administración:** Usuarios · Perfil · Configuración · Registro de actividad

### Notas específicas de este proyecto
- **Niveles:** casi todo se filtra por `nivel` (PRIMARIA/SECUNDARIA/PREUNIVERSITARIO). Los DTO y
  servicios Angular ya contemplan el nivel; respétalo en cada módulo nuevo.
- **Imágenes (foto de profesor/alumno, logo, galería):** hoy se sirven como bytes por endpoints
  server-side. En Angular se muestran con `<img [src]>` apuntando a esos endpoints, o migrándolos
  a `/api/...` que devuelvan la imagen; decide caso por caso.
- **PDF y QR:** mantenlos en el backend (ya usan flying-saucer/zxing). Exponer un endpoint
  `/api/.../pdf` que devuelva `application/pdf` y descargarlo desde Angular como `Blob`.
- **Soft delete:** `Alumno/Profesor/Curso/Ciclo` usan el flag `eliminado`; los servicios ya lo
  respetan, la API no necesita lógica extra.
