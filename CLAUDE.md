# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

"Sistema de matrículas - Academia Horizonte" — a server-rendered Spring Boot 3.5 / Java 21 web app for managing student enrollment at an academy: students (`Alumno`), teachers (`Profesor`), courses (`Curso`), academic terms (`Ciclo`), schedules (`Horario`), enrollments (`Matricula`/`MatriculaDetalle`), and payments (`Pago`). UI text, domain names, and messages are in Spanish.

## Build, run, test

This is a Maven project; use the wrapper (no need for a local Maven install).

```
./mvnw spring-boot:run          # run the app (listens on port 9094)
./mvnw clean package            # build a jar (target/*.jar)
./mvnw test                     # run all tests
./mvnw test -Dtest=ClassName    # run a single test class
./mvnw test -Dtest=ClassName#methodName   # run a single test method
```

On Windows use `mvnw.cmd` instead of `./mvnw` if not running under Git Bash/PowerShell wrapper resolution.

There is essentially one test currently (`ProjectApplicationTests`, a Spring context-load smoke test), so `spring-boot:run` plus manual verification in the browser is the primary way changes get checked.

**Database**: requires a running MySQL instance. `src/main/resources/application.yml` reads connection details from environment variables (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `PORT`, `SHOW_SQL`, `THYMELEAF_CACHE`), each with a local-dev default baked in (`jdbc:mysql://localhost:3306/Academia`, user `root`) so `./mvnw spring-boot:run` works out of the box locally; override the env vars for any other environment. `spring.jpa.hibernate.ddl-auto: update`, so schema evolves automatically from entity changes; there are no migration scripts (no Flyway/Liquibase) — see the `model/` note below for what that means in practice. On first run, `DataInitializer` seeds three roles (`ROLE_ADMIN`, `ROLE_CAJERO`, `ROLE_AUXILIAR`) and three users (`admin`/`admin123`, `cajero`/`cajero123`, `auxiliar`/`auxiliar123`) if they don't already exist.

## Architecture

Classic layered Spring MVC + Thymeleaf app, no REST/JSON API — controllers return view names, forms post back via `@RequestParam`/model-attribute binding.

- `controller/` — `@Controller` classes, one per module (`AlumnoController`, `CursoController`, `MatriculaController`, etc.), each mapped under a plural path (`/alumnos`, `/cursos`, ...). Return Thymeleaf view names like `"alumnos/lista"` / `"alumnos/formulario"`, or `redirect:/...`.
- `service/` + `service/impl/` — interface/implementation split. Business rules and transactional boundaries (`@Transactional`) live in `*ServiceImpl`, not in controllers or repositories.
- `repository/` — Spring Data JPA interfaces. Custom queries (e.g. `findAllConEstudianteYSemestre`, `findByIdConDetalle` in `MatriculaRepository`) fetch aggregate roots with eager joins for detail pages, since entity associations default to `FetchType.LAZY`.
- `model/` — JPA entities. Table/column physical names match the Java class/field names (`alumnos`, `profesores`, `ciclos`, `alumnos.area`, `cursos.profesor_id`, `matriculas.alumno_id`, `matriculas.ciclo_id`, etc.) — the tables were renamed directly against the database (`RENAME TABLE`/`RENAME COLUMN`, preserving data) so that `ddl-auto=update` wouldn't create orphaned duplicate tables, and the `@Table`/`@Column`/`@JoinColumn` annotations were updated to match in the same change. **If you're working against a database that predates this rename** (still has `estudiantes`/`docentes`/`semestres`/`carrera`/`docente_id`/`estudiante_id`/`semestre_id`), it needs the same one-time `RENAME TABLE`/`RENAME COLUMN` treatment before the app will start cleanly — don't just let Hibernate create fresh tables under the new names, that orphans the old data. One remaining bit of drift: `Matricula`'s Java *fields* are still named `estudiante` (type `Alumno`) and `semestre` (type `Ciclo`) rather than `alumno`/`ciclo` — renaming those would touch the JPQL derived-query method names (`findByEstudianteId...`) and every template binding (`m.estudiante`, `m.semestre`), so it was left out of scope; keep that in mind when searching for "alumno" or "ciclo" usage on `Matricula`.
- `dto/` — `*Form` classes used for Bean Validation (`@Valid`) on create/edit forms, separate from the JPA entities.
- `exception/` + `controller/GlobalExceptionHandler.java` — `RecursoNoEncontradoException` and `IllegalArgumentException`/`IllegalStateException` are caught centrally and turned into a redirect back to the current module's list view with a flash error message (`mensajeError`), rather than an error page. `GlobalExceptionHandler.rutaBaseDeModulo` derives the redirect target from the request URI against a hardcoded list of module prefixes — add new modules there too if their controllers should get this fallback behavior.
- `config/SecurityConfig.java` — Spring Security filter chain; authorization is role-based per URL pattern (`hasRole("ADMIN")`, `hasAnyRole("ADMIN", "CAJERO")`), configured per-path rather than per-method/annotation. Check this file when adding new controller endpoints that should be restricted, especially write actions (`/nuevo`, `/guardar`, `/editar/**`, `/eliminar/**`), which are consistently admin/cajero-gated while read (`GET`) endpoints are just `authenticated()`.
- `config/DataInitializer.java` — seeds default roles/users on startup (see above).
- `templates/` — Thymeleaf templates, one directory per module (`alumnos/`, `matriculas/`, ...) each typically with `lista.html` (list) and `formulario.html` (create/edit form), plus shared `layout.html` and `fragments/` (menu, sidebar) via `thymeleaf-layout-dialect`. `matriculas/ficha.html` / `ficha-pdf.html` render an enrollment receipt; the PDF variant is rendered server-side to bytes via `flying-saucer-pdf`'s `ITextRenderer` in `MatriculaController.fichaPdf`.

### Enrollment domain notes

`Matricula` (enrollment) is the most complex aggregate: one `Matricula` per (student, `Ciclo`) has many `MatriculaDetalle` (enrolled courses) and many `Pago` (payments, e.g. matrícula fee + pensión installments). `MatriculaServiceImpl.matricular(...)` is upsert-like: if a `Matricula` already exists for that student+ciclo, it replaces the course details (deleting old `MatriculaDetalle` rows directly via repository + `flush()` before clearing the in-memory collection — required because of the unique constraint on `(matricula_id, curso_id)`) rather than creating a new enrollment record. `MatriculaService.agregarCuota(...)` adds a new `Pago` to an already-existing `Matricula` (e.g. next month's pensión) without touching enrollment/course details.

### Soft delete

`Alumno`/`Profesor`/`Curso`/`Ciclo` use a boolean `eliminado` flag for logical deletion rather than removing rows; check for existing filtering-by-`eliminado` conventions in the corresponding repository/service before adding new queries against those tables.

## Implemented beyond the original scaffold

The app has grown well past its initial commit. These are already in place — **check here before re-adding them**:

- **Success/error flash messages** on every module (`mensajeExito`/`mensajeError`, rendered once in `layout.html`), plus a dedicated `templates/error/404.html` / `500.html`.
- **Server-side search + pagination** on every list view (`alumnos`, `cursos`, `profesores`, `ciclos`, `matriculas`, `pagos`, `usuarios`), all following the same shape: repository `buscar(q, Pageable)` with a `@Query`/`countQuery` pair, controller `@RequestParam(required=false) String q` + `@PageableDefault`, a `.searchbar` block in the template, and `fragments/paginacion :: paginacion`. Several services also keep an unpaginated `listarTodos()` — that's not redundant, it feeds pickers embedded in *other* forms (e.g. the course checklist in `matriculas/formulario.html`) which must stay unpaginated.
- **Click-to-select searchable pickers** for single-choice fields with many rows (alumno, ciclo, curso, profesor), replacing plain `<select>` dropdowns — a hidden input holds the real value, and a `.courselist`/`.courserow`-style list of clickable rows is filtered live by a search box, so picking is a single click instead of open-dropdown-then-scroll. Shared JS lives in `static/js/buscador-select.js`.
- **Mobile navigation.** The sidebar becomes a slide-in drawer with a hamburger toggle under ~920px; tables scroll horizontally instead of breaking layout.
- **Reports & export** (`ReporteController`): PDF (flying-saucer) and Excel (Apache POI) for alumnos por ciclo/turno, ingresos por mes, and alumnos morosos.
- **Dashboard chart** on `inicio.html` via Chart.js (ingresos por mes).
- **Automatic overdue payments**: `scheduler/PagoScheduler` runs daily (`@Scheduled(cron = "0 0 1 * * *")`) and flips unpaid `Pago`s past their `fechaVencimiento` to `VENCIDO` via `PagoService.marcarVencidos()`.
- **Student expediente** (`/alumnos/{id}/expediente`): every `Matricula` for that student, its cursos, and its pagos, plus an inline "+ Agregar cuota" form.
- **Partial payments.** `Pago` has `montoPagado` (nullable column, treat `null` as `ZERO`) and a computed `getSaldo()`; a fourth `estado` value `PARCIAL` sits between `PENDIENTE` and `PAGADO`. Each payment/abono is its own `Abono` row (`model/Abono.java`: monto, fecha, método, `registradoPor`) rather than overwriting `Pago` directly — `PagoService.registrarAbono(...)` is the only way to move money against a `Pago`. Monthly income reporting (`ReporteServiceImpl.ingresosPorMes`) sums `Abono` rows (real cash received), not `Pago.monto` — don't switch that back to summing pagos, it would double- or under-count once partial payments exist.
- **Extra alumno fields**: `dni` (required, 8 digits), `nombrePadre`/`telefonoPadre` (optional). `email` is optional (nullable, still unique when present) — always null-check/`?: '-'` it in templates, and skip the duplicate-check in `AlumnoServiceImpl.guardar()` when blank.

## Still open

- `HorarioController.guardar` binds straight from `@RequestParam`s onto the `Horario` entity rather than through a validated `dto/*Form` — it's not unvalidated (`HorarioServiceImpl.guardar` checks hora-fin-after-hora-inicio and schedule-overlap conflicts server-side), just a different pattern (service-layer validation, not Bean Validation) from the rest of the `*Form` + `@Valid` modules. Every other module (`Alumno`, `Curso`, `Profesor`, `Usuario`, `Ciclo`) does use a validated `*Form`.
- `Matricula`'s Java field names (`estudiante`, `semestre`) still don't match the `Alumno`/`Ciclo` types they hold — see the `model/` note above.

## Stray items

- A top-level `org/springframework/boot/diagnostics/annotations.xml` directory exists at the repo root (outside `src/`) — this is not part of the Maven source layout; don't treat it as an active source location.
