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

**Database**: requires a running MySQL instance. Connection is hardcoded in `src/main/resources/application.yml` — `jdbc:mysql://localhost:3306/proyect`, user `root`. `spring.jpa.hibernate.ddl-auto: update`, so schema evolves automatically from entity changes; there are no migration scripts (no Flyway/Liquibase). On first run, `DataInitializer` seeds two roles (`ROLE_ADMIN`, `ROLE_CAJERO`) and two users (`admin`/`admin123`, `cajero`/`cajero123`) if they don't already exist.

## Architecture

Classic layered Spring MVC + Thymeleaf app, no REST/JSON API — controllers return view names, forms post back via `@RequestParam`/model-attribute binding.

- `controller/` — `@Controller` classes, one per module (`AlumnoController`, `CursoController`, `MatriculaController`, etc.), each mapped under a plural path (`/alumnos`, `/cursos`, ...). Return Thymeleaf view names like `"alumnos/lista"` / `"alumnos/formulario"`, or `redirect:/...`.
- `service/` + `service/impl/` — interface/implementation split. Business rules and transactional boundaries (`@Transactional`) live in `*ServiceImpl`, not in controllers or repositories.
- `repository/` — Spring Data JPA interfaces. Custom queries (e.g. `findAllConEstudianteYSemestre`, `findByIdConDetalle` in `MatriculaRepository`) fetch aggregate roots with eager joins for detail pages, since entity associations default to `FetchType.LAZY`.
- `model/` — JPA entities. Tables sometimes keep old physical names for backward compatibility with existing data even though the Java field/domain name has moved on — e.g. `Alumno` maps to table `estudiantes` and column `carrera` is now exposed as Java field `area`; `Matricula.estudiante`/`Matricula.semestre` are commented as kept "por compatibilidad". When touching these entities, don't rename columns/tables to match Java names — check for this kind of intentional drift first.
- `dto/` — `*Form` classes used for Bean Validation (`@Valid`) on create/edit forms, separate from the JPA entities.
- `exception/` + `controller/GlobalExceptionHandler.java` — `RecursoNoEncontradoException` and `IllegalArgumentException`/`IllegalStateException` are caught centrally and turned into a redirect back to the current module's list view with a flash error message (`mensajeError`), rather than an error page. `GlobalExceptionHandler.rutaBaseDeModulo` derives the redirect target from the request URI against a hardcoded list of module prefixes — add new modules there too if their controllers should get this fallback behavior.
- `config/SecurityConfig.java` — Spring Security filter chain; authorization is role-based per URL pattern (`hasRole("ADMIN")`, `hasAnyRole("ADMIN", "CAJERO")`), configured per-path rather than per-method/annotation. Check this file when adding new controller endpoints that should be restricted, especially write actions (`/nuevo`, `/guardar`, `/editar/**`, `/eliminar/**`), which are consistently admin/cajero-gated while read (`GET`) endpoints are just `authenticated()`.
- `config/DataInitializer.java` — seeds default roles/users on startup (see above).
- `templates/` — Thymeleaf templates, one directory per module (`alumnos/`, `matriculas/`, ...) each typically with `lista.html` (list) and `formulario.html` (create/edit form), plus shared `layout.html` and `fragments/` (menu, sidebar) via `thymeleaf-layout-dialect`. `matriculas/ficha.html` / `ficha-pdf.html` render an enrollment receipt; the PDF variant is rendered server-side to bytes via `flying-saucer-pdf`'s `ITextRenderer` in `MatriculaController.fichaPdf`.

### Enrollment domain notes

`Matricula` (enrollment) is the most complex aggregate: one `Matricula` per (student, `Ciclo`) has many `MatriculaDetalle` (enrolled courses) and many `Pago` (payments, e.g. matrícula fee + pensión installment). `MatriculaServiceImpl.matricular(...)` is upsert-like: if a `Matricula` already exists for that student+ciclo, it replaces the course details (deleting old `MatriculaDetalle` rows directly via repository + `flush()` before clearing the in-memory collection — required because of the unique constraint on `(matricula_id, curso_id)`) rather than creating a new enrollment record.

### Soft delete

`Alumno` uses a boolean `eliminado` flag for logical deletion rather than removing rows; check for existing filtering-by-`eliminado` conventions in `AlumnoRepository`/`AlumnoServiceImpl` before adding new queries against that table.

## Stray items

- A top-level `org/springframework/boot/diagnostics/annotations.xml` directory exists at the repo root (outside `src/`) — this is not part of the Maven source layout; don't treat it as an active source location.