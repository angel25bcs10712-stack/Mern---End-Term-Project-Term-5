# CodeRoute

CodeRoute is an adaptive DSA learning platform for interview preparation. The project contains a React/Vite frontend, a Spring Boot API, PostgreSQL persistence, and JWT-based authentication. Learning CRUD workflows and recommendation logic are not implemented yet.

## Project layout

```text
frontend/   React, Vite, Tailwind CSS, and React Router
backend/    Spring Boot REST API, Spring Data JPA, Flyway migrations, PostgreSQL
```

## Prerequisites

- Node.js 20.19+ or 22.12+
- Java 21+
- PostgreSQL 16+ for database-backed development

## Run the frontend

```powershell
cd frontend
npm install
Copy-Item .env.example .env.local
npm run dev
```

Vite serves the application at `http://localhost:5173`. `VITE_API_BASE_URL` points the frontend to the backend; its default is `http://localhost:8080`.

## Configure the database

The backend defaults to an in-memory H2 database for local startup, so it can boot without a PostgreSQL installation. Set `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` to use a real PostgreSQL database when you want the production-like configuration.

For PostgreSQL, create a database and a dedicated local user in `psql` (choose your own password):

```sql
CREATE USER postgres WITH PASSWORD 'choose-your-local-password';
CREATE DATABASE coderoute OWNER postgres;
```

The backend reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from its process environment. `.env.example` documents the variables but Spring Boot does not load `.env` files automatically. In PowerShell, set the values in the terminal where you will run the API:

```powershell
$env:DB_URL = "jdbc:postgresql://localhost:5432/coderoute"
$env:DB_USERNAME = "postgres"
$env:DB_PASSWORD = "your-local-password"
$env:FRONTEND_ORIGIN_PATTERNS = "http://localhost:*,http://127.0.0.1:*"
$env:FLYWAY_ENABLED = "true"
$env:JWT_SECRET = "replace-with-a-random-secret-of-at-least-32-characters"
```

`JWT_SECRET` is required and must contain at least 32 UTF-8 bytes. Keep it out of source control and use a randomly generated secret outside local development. Tokens expire after 24 hours by default; set `JWT_EXPIRATION` (or the legacy `JWT_EXPIRATION_MS`) to change this.

Flyway applies `backend/src/main/resources/db/migration/V1__create_learning_schema.sql` on startup. Hibernate validates the migrated schema and does not create or modify tables. A reachable PostgreSQL database with this schema is required for the backend to start. The no-database test configuration disables both Flyway and schema validation; disabling Flyway alone is not enough for a normal application startup.

The model is under `backend/src/main/java/com/coderoute/entity`, repositories under `repository`, and validated request/safe response records under `dto`. Passwords are BCrypt-hashed in `password_hash`; user responses never include the hash. Authenticated requests use `Authorization: Bearer <token>`. Private endpoints require authentication, `/api/admin/**` requires the `ADMIN` role, and `/api/auth/me` only returns the authenticated user's own profile. New user-data handlers must scope repository access to the authenticated user's ID.

## Authentication API

- `POST /api/auth/register` accepts `{ "name", "email", "password" }`, creates a `USER`, and returns `201` with `{ "token", "user" }`. Email must be valid and passwords must be 8-72 characters. Duplicate email returns `409`.
- `POST /api/auth/login` accepts `{ "email", "password" }` and returns `200` with `{ "token", "user" }`; invalid credentials return `401`.
- `GET /api/auth/me` returns the current safe user DTO and requires a valid JWT.

The frontend provides `/login`, `/register`, and a protected `/account` route. It persists the JWT in browser local storage and clears it on sign out.

## Problem practice API

All problem and topic endpoints require a JWT. `GET /api/problems` accepts `page` (zero-based, default `0`), `size` (default `20`, maximum `50`), `search`, `topicId`, `difficulty` (`BEGINNER`, `INTERMEDIATE`, `ADVANCED`), and `solved` (`true`/`false`). Search and solved status are evaluated by the backend; the response contains page metadata and DTOs. `GET /api/problems/{id}` returns the brief and caller-specific solved state. `GET /api/topics` returns the topic filter options.

`POST /api/problems/{id}/attempt` and `POST /api/problems/{id}/solve` accept `{ "timeTakenSeconds": 900, "attempts": 2 }`. The caller's identity is taken only from the JWT. `GET /api/users/me/progress` returns the caller's aggregate counts and per-topic progress. Flyway migration V2 seeds the topic list and original short problem briefs.

## Run the backend

From the repository root:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

The backend runs independently from the frontend at `http://localhost:8080`. With PostgreSQL configured, Flyway creates or upgrades the schema before the API starts.

Verify it with:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
```

Expected response:

```json
{
  "status": "UP"
}
```

Run the backend checks with:

```powershell
cd backend
.\mvnw.cmd test
```