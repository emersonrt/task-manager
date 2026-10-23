# Task Manager API

A small REST API for tasks organized by categories, built with Java 21 and Spring Boot 3.5.

The domain is intentionally simple. The point of this project is how a small service should be built: a clear HTTP contract, consistent errors, a schema that enforces its own rules, and tests at the right levels.

## Highlights

- **Consistent errors.** Every error is an [RFC 7807](https://www.rfc-editor.org/rfc/rfc7807) problem detail (`application/problem+json`), including field-level validation errors.
- **Correct status codes.** `201` with a `Location` header on create, `204` on delete, `404` for missing resources, `409` for conflicts, `422` when the body points to a category that does not exist.
- **The database guards its own rules.** Flyway migrations add a check constraint for task status, a case-insensitive unique index for category names, and indexes for the filters the API exposes. Service checks give friendly messages, and the constraints catch the races those checks cannot see.
- **Pagination and filtering.** List endpoints are paginated (page size capped at 100) and tasks can be filtered by status and category.
- **No N+1 on listing.** The task to category relation is lazy, and responses only need the category id, which comes from the foreign key.
- **Tests at three levels**, described below.

## Tech stack

Java 21, Spring Boot 3.5 (Web, Validation, Data JPA, Actuator), PostgreSQL 17, Flyway, springdoc-openapi, JUnit 5, Mockito, AssertJ, Zonky Embedded Database, Docker, GitHub Actions.

## API

Base path: `/api`. Interactive docs: `http://localhost:8080/api/swagger-ui.html`.

| Method | Path | Description |
|---|---|---|
| `GET` | `/categories?page=&size=&sort=` | List categories (sorted by name by default) |
| `GET` | `/categories/{id}` | Get a category |
| `POST` | `/categories` | Create a category |
| `PUT` | `/categories/{id}` | Replace a category |
| `DELETE` | `/categories/{id}` | Delete a category (`409` if it still has tasks) |
| `GET` | `/tasks?status=&categoryId=&page=&size=&sort=` | List tasks, newest first by default |
| `GET` | `/tasks/{id}` | Get a task |
| `POST` | `/tasks` | Create a task |
| `PUT` | `/tasks/{id}` | Replace a task |
| `DELETE` | `/tasks/{id}` | Delete a task |

Task status is one of `TODO`, `IN_PROGRESS`, `DONE`.

Example:

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H 'Content-Type: application/json' \
  -d '{"title": "Write README", "status": "TODO"}'
```

Validation error:

```json
{
  "type": "about:blank",
  "title": "Bad Request",
  "status": 400,
  "detail": "Request validation failed",
  "instance": "/api/tasks",
  "errors": [
    { "field": "title", "message": "Title is required" }
  ]
}
```

## Running locally

Requirements: Java 21 and Docker.

```bash
./gradlew bootRun
```

Spring Boot's Docker Compose support starts PostgreSQL from `compose.yaml` automatically.

To run the application itself in a container as well:

```bash
docker compose --profile app up --build
```

Defaults work out of the box. To change credentials, copy `.env-example` to `.env`.

## Tests

```bash
./gradlew test
```

No Docker is needed to run the tests.

| Level | What it covers | Tools |
|---|---|---|
| Unit | Business rules in the services (duplicates, missing references, deletes with dependents) | JUnit 5, Mockito, AssertJ |
| Web slice | HTTP contract: status codes, validation, problem details, `Location` header | `@WebMvcTest`, MockMvc |
| Integration | Full requests against a real PostgreSQL with the real Flyway migrations, including constraints, filters and pagination | `@SpringBootTest`, Zonky Embedded Database |

Integration tests use Zonky's embedded PostgreSQL binaries and reset the database from a template after each test, so they are isolated and fast. Upstream now prefers its Docker provider and marks the embedded one as deprecated. Switching only requires changing `zonky.test.database.provider` in `src/test/resources/application.properties`.

## Project structure

```
src/main/java/com/emersonrt/taskmanager
├── category
│   ├── in/web            REST controller (inbound adapter)
│   ├── out/persistence   Spring Data repository (outbound adapter)
│   ├── service           Business rules and transactions
│   ├── domain            JPA entity
│   └── dto               Request and response records, entity to response mapper
├── task                  Same layout as category, plus the TaskStatus enum in domain
└── common
    ├── error             Domain exceptions and the global problem detail handler
    └── web               Shared web types (page envelope, OpenAPI config)
```

Code is organized by feature first, so everything about tasks lives under `task`. Inside a feature, `in` holds what calls the application (today only HTTP) and `out` holds what the application calls (today only the database), so a message consumer or producer would get its own package next to `web` or `persistence`.

## What I would add next

- Optimistic locking with `ETag` and `If-Match` to prevent lost updates
- Authentication and per-user tasks
- Metrics and tracing with Micrometer and OpenTelemetry
