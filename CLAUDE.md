# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project

Spring Boot 3.5 REST API (Java 21, PostgreSQL, Flyway). Portfolio project: small scope, but the code must stay clean, consistent and fully tested. Everything is in English: code, comments, commit messages, docs and API messages.

## Commands

- `./gradlew test` runs all tests (no Docker needed)
- `./gradlew build` compiles, tests and builds the jar
- `./gradlew bootRun` runs the app and starts PostgreSQL from `compose.yaml` (needs Docker)

## Architecture

- Packages by feature first (`category`, `task`, and `common` for shared code), then by role inside each feature:
  - `in/web`: inbound adapters (REST controllers). New entry points such as message consumers go under `in/<kind>`.
  - `out/persistence`: outbound adapters (Spring Data repositories). New outputs such as message producers go under `out/<kind>`.
  - `service`: business rules and transactions.
  - `domain`: JPA entities and enums.
  - `dto`: request and response records plus the mapper from entities to responses.
- Controllers only handle HTTP. Business rules live in services. Controllers never touch repositories or entities directly.
- Services are `@Transactional(readOnly = true)` at class level. Write methods add `@Transactional`.
- When a response depends on generated values (ids, timestamps) or a constraint may fail, flush through the repository before mapping (`saveAndFlush` or `repository.flush()`).
- Entities have no public setters. Changes go through intent methods such as `update(...)`.
- DTOs are records. Validation annotations go on request DTOs, not on entities.

## API conventions

- Errors are RFC 7807 `ProblemDetail`, built in `common/error/GlobalExceptionHandler`. Never return ad hoc error bodies.
- Status codes: `201` plus `Location` on create, `204` on delete, `404` missing resource, `409` conflict, `422` reference to a missing related resource, `400` invalid input.
- List endpoints return `PageResponse` and accept `page`, `size` and `sort`.

## Database

- Schema changes only through a new Flyway migration in `src/main/resources/db/migration`. Never edit an applied migration.
- Hibernate runs with `ddl-auto: validate`, so entities must match the schema.
- Rules that matter for consistency also exist as constraints in the database.

## Tests

- Unit tests (Mockito) for service rules.
- `@WebMvcTest` for the HTTP contract of each controller.
- Integration tests are annotated with `@IntegrationTest` (Zonky embedded PostgreSQL, real migrations, database reset after each test).
- Every behavior change comes with tests. Test names describe the behavior, for example `refusesToDeleteCategoryWithTasks`.

## Git

- Branches: `<type>/<short-description>`, for example `feat/task-due-dates` or `fix/category-conflict-status`.
- Commits follow Conventional Commits: `<type>(<optional scope>): <imperative summary>`. Types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `ci`, `chore`.
- One logical change per commit. Pull requests describe the why, the what and how it was tested.
