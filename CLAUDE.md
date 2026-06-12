# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build the project
./mvnw clean compile

# Run the application (dev profile by default, port 8080)
./mvnw spring-boot:run

# Package for deployment
./mvnw clean package

# Run all tests (⚠️ currently 0 tests exist — see "Test Coverage" below)
./mvnw test

# Quick rebuild (reflects Thymeleaf/static changes in target/classes/)
./mvnw compile
```

**Prerequisites:** JDK 17+, MySQL 8.0+
**Database:** Create a `taskflow` database first, or run `src/main/resources/schema.sql`
**Access:** http://localhost:8080
**Default accounts:** `admin / admin123` (ADMIN), `demo / demo123` (USER)
**Maven Wrapper:** Use `./mvnw` (no system Maven install required). The wrapper jar is at `.mvn/wrapper/maven-wrapper.jar`.

### Dev Workflow

- **Thymeleaf/JS/CSS changes** require `./mvnw compile` (or `Ctrl+F9` in IntelliJ) to copy files to `target/classes/` where Spring Boot reads them.
- **Java changes** trigger DevTools auto-restart (only recompiled classes, ~2–3s).
- **Logs** at `logs/taskflow.log` with DEBUG level for `com.taskflow` in dev profile.
- **H2** is also on classpath for lightweight testing without MySQL.
- **Data seeding:** `DataInitializer` (CommandLineRunner) auto-creates `admin/admin123` and `demo/demo123` users if DB is empty. No need to run seed SQL manually.

### Test Coverage

**This project currently has zero tests** — no unit tests, no integration tests. When adding tests:
- Place them in `src/test/java/com/taskflow/` (standard Maven layout, already on classpath from `pom.xml`)
- `spring-boot-starter-test` and H2 are already in dependencies for test isolation
- Use `@SpringBootTest` + `@AutoConfigureMockMvc` for controller integration tests
- Service-layer tests can use `@ExtendWith(MockitoExtension.class)`

## Architecture

Standard Spring Boot 3.2 layered architecture with Thymeleaf server-side rendering.

### Package Layout

```
com.taskflow/
├── config/          # SecurityConfig, WebMvcConfig, SchedulingConfig, DataInitializer
├── controller/      # Thin controllers — delegates to services
│   ├── admin/       # Admin-only endpoints (hasRole('ADMIN'))
│   └── rest/        # AJAX REST endpoints (/api/*)
├── service/         # Interface + Impl pattern
│   └── impl/        # Business logic lives here
├── dao/             # ALL Spring Data JPA repositories (NOT "repository/" — non-standard naming)
├── entity/          # JPA entities with @PrePersist/@PreUpdate timestamps
│   └── enums/       # Role, TaskStatus, TaskPriority — stored as String in DB
├── dto/             # Request/response objects; some carry @Valid constraints
├── exception/       # GlobalExceptionHandler + custom exceptions
├── interceptor/     # LoggingInterceptor (request logging) + PageVisitInterceptor (analytics)
├── mapper/          # (planned) Entity-DTO mapping
└── validator/       # (planned) Custom validation annotations
```

### Template Layout (Thymeleaf)

```
templates/
├── layout/base.html       # Main layout — navbar, alerts, footer, CSRF meta tags, main.js
├── users/                 # User-facing pages (NOT "user/")
│   ├── dashboard.html     # Dashboard with Chart.js doughnut
│   ├── projects.html      # Project listing
│   ├── project-detail.html # Project detail with task list + members
│   ├── project-form.html  # Create/edit project
│   ├── task-detail.html   # Task detail with comments, attachments, AJAX status
│   ├── task-form.html     # Create/edit task
│   └── profile.html       # User profile
├── admin/                 # Admin pages (all use the same layout/base.html with content="admin/...")
│   │                       #   dashboard, users, user-form, projects, tasks, logs
├── auth/                  # login.html, register.html (standalone, no base layout)
└── error/                 # 403.html, 404.html, 500.html, error.html
```

### Static Assets

```
static/
├── css/
│   ├── bootstrap.min.css     # Bootstrap 5 (local)
│   ├── all.min.css           # Font Awesome 6 (local)
│   └── style.css             # Custom CSS — CSS variables, gradient cards, priority classes
├── js/
│   ├── bootstrap.bundle.min.js
│   └── main.js              # CSRF helpers (getCsrfToken/getCsrfHeader), AJAX utils
├── images/
│   ├── empty-projects.svg   # Shown when project list is empty
│   ├── empty-tasks.svg      # Shown when task list is empty
│   └── login-hero.svg       # Decorative illustration on login page
└── favicon.svg              # Site favicon (inline SVG, no .ico)
```

All user-facing pages use the **fragment layout pattern**: controllers set `model.addAttribute("content", "users/...")` and return `"layout/base"`.

### Key Design Decisions

- **Auth:** Spring Security with form login, BCrypt, role-based (`ROLE_ADMIN`/`ROLE_USER`). Remember-Me via `persistent_logins` table.
- **DB schema:** Hibernate `ddl-auto: update` in dev. 9 tables: users, projects, project_members, tasks, comments, task_attachments, visit_logs, audit_logs, persistent_logins.
- **Entities:** Lombok (`@Data`, `@Builder`, `@NoArgs`/`@AllArgs`). Manual `@PrePersist`/`@PreUpdate` for timestamps (not JPA Auditing).
- **open-in-view: false** — lazy loading throws outside `@Transactional`. Fetch data eagerly or within service transactions.
- **Service layer:** Interface + Impl. `@Transactional` at impl methods. Custom exceptions: `ResourceNotFoundException`, `UnauthorizedException`, `BadRequestException`, `FileStorageException`.
- **AJAX:** `TaskRestController` handles inline status updates via POST `/api/tasks/{id}/status`. CSRF token passed via meta tags (`_csrf.token`, `_csrf.headerName`), helpers in `main.js` (`getCsrfToken()`, `getCsrfHeader()`).
- **Native queries:** `AdminProjectController.resetProjectId()` uses `EntityManager.createNativeQuery("ALTER TABLE projects AUTO_INCREMENT = 1")` directly. Available pattern for DDL or vendor-specific SQL when JPQL can't express it.
- **Global error handling:** `GlobalExceptionHandler` (@ControllerAdvice) maps exceptions to custom error templates: `error/403`, `error/404`, `error/500`, `error/error` (400). Validation errors from `@Valid` DTOs are caught and rendered as comma-separated messages.
- **Audit logging:** `AuditLogService` writes to `audit_logs` table. Injected into service impls and admin controllers at key mutation points (create/delete entities, status changes, role changes, member management). Viewable at `/admin/logs`. `ScheduledTaskService` cleans entries older than 90 days.
- **Pagination:** Spring Data `Pageable` passed through controllers. Pagination fragment rendered with Thymeleaf.
- **File upload:** Disk storage at `app.upload-dir` (default `{user.dir}/uploads`). UUID filenames. Max 10MB. Allowed extensions configured in `application.yml`.
- **Static assets:** Bootstrap 5, Font Awesome 6 served locally from `/css/` and `/js/` (no CDN/webjars). Custom CSS in `style.css` with CSS variables, gradient stat cards, priority classes (`priority-LOW`, `priority-MEDIUM`, `priority-HIGH`, `priority-URGENT`).
- **Profiles:** `dev` (default) — DEBUG logging, cache off, ddl-auto=update. `prod` — INFO logging, cache on, ddl-auto=validate.
- **Data seeding:** `DataInitializer` (CommandLineRunner) creates admin + demo users when DB is empty.
- **Scheduling:** `ScheduledTaskService` cleans old audit logs (90d) and visit logs (30d) daily at 3 AM.

### Entity Relationships

```
User ──1:N── Project (owner)
User ──N:M── Project (members via project_members table)
Project ──1:N── Task
Task ──N:1── User (creator) ──N:1── User (assignee)
Task ──1:N── Comment ──N:1── User (author)
Task ──1:N── TaskAttachment ──N:1── User (uploadedBy)
```

All `@ManyToOne` / `@OneToMany` use `FetchType.LAZY`. N+1 mitigated by `@EntityGraph` or `JOIN FETCH` where needed.

### Route Patterns

| Pattern | Access | Description |
|---------|--------|-------------|
| `/login`, `/register` | Public | Auth (standalone templates) |
| `/dashboard` | Authenticated | User dashboard with Chart.js |
| `/projects/**` | Authenticated | Project CRUD + members |
| `/tasks/**` | Authenticated | Task CRUD + comments + attachments |
| `/api/tasks/**` | Authenticated | AJAX endpoints (status updates) |
| `/profile` | Authenticated | Edit profile |
| `/admin/**` | ADMIN role | Admin dashboard, user/project/task management, logs |

### Thymeleaf Gotchas

- **SpEL can't compare enums:** `th:if="${s == task.status}"` fails. Use `s.name() == task.status.name()` instead.
- **`th:class` replaces all classes:** Use `class="..."` + `th:classappend="..."` to preserve base classes.
- **Template changes** must be copied to `target/classes/templates/` (or run `mvn compile`). DevTools restart doesn't pick up template-only changes.

### Application Config (application.yml)

- MySQL: `jdbc:mysql://localhost:3306/taskflow` (root/123456)
- Multipart: max 10MB file and request size
- Jackson: Asia/Shanghai timezone, `yyyy-MM-dd HH:mm:ss` format
- Upload: default `{user.dir}/uploads`, allowed extensions in `app.allowed-extensions`
- Logging: file at `logs/taskflow.log`, 10MB rotation, 7-day history
