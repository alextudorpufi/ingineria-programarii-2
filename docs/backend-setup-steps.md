# Backend setup steps

The backend uses Java 21, Spring Boot, Maven, PostgreSQL 18, Spring Data JPA, and Flyway. Run Spring Boot locally and PostgreSQL in Docker using the repository's `compose.yaml`.

## 1. Install prerequisites

Install JDK 21 and Docker Desktop with Docker Compose. Start Docker Desktop, then verify:

```bash
java -version
docker --version
docker compose version
```

Use the Maven Wrapper included in `backend/`; a separate Maven installation is unnecessary.

## 2. Prepare the Spring Boot project

When working from this repository, use the existing `backend/` project and its pinned dependencies.

If initializing the backend from scratch, use [Spring Initializr](https://start.spring.io/) with Maven, Java 21, Jar packaging, group `ro.marketplace`, artifact `backend`, and package `ro.marketplace`. Choose a stable Spring Boot release and extract the project into `backend/`.

Include Spring Web, Spring Data JPA, PostgreSQL Driver, Flyway Migration, Validation, and Spring Boot Actuator. Ensure PostgreSQL-specific Flyway support (`org.flywaydb:flyway-database-postgresql`) is included. Add Spring Security and WebSocket when implementing authentication and chat.

## 3. Configure the local environment

From the repository root, copy the example configuration if `.env` does not already exist:

```bash
cp .env.example .env
```

Check the values for `DB_NAME`, `DB_USER`, `DB_PASSWORD`, and `DB_PORT`. The default database port is `5432`. Keep `.env` local and commit only `.env.example`.

Ensure port `5432` is available. If another PostgreSQL instance is using it, stop that instance before starting the project's database. On macOS, inspect the listener with:

```bash
lsof -nP -iTCP:5432 -sTCP:LISTEN
```

## 4. Start PostgreSQL

From the repository root:

```bash
docker compose up -d db
docker compose ps
```

Wait for the `db` service to report `healthy`. To investigate startup issues:

```bash
docker compose logs db
```

Compose reads `.env` automatically. The database uses a named volume to preserve data between container restarts. Database credentials are applied when the volume is first initialized; changing `.env` later does not update existing database users or passwords.

## 5. Configure and run Spring Boot

The existing `backend/src/main/resources/application.yaml` reads the database settings from environment variables and connects to PostgreSQL on `localhost`. Keep Flyway enabled and Hibernate's `ddl-auto` set to `validate`.

Spring Boot does not automatically read the root `.env`. In a macOS/Linux terminal, load it from the repository root before starting the backend:

```bash
set -a
source .env
set +a

cd backend
./mvnw spring-boot:run
```

Repeat the environment-loading step in each new terminal used to run the backend. If running from an IDE, set the same database variables in its run configuration. On Windows, configure those variables in the IDE or shell and use `mvnw.cmd`.

## 6. Verify the backend

Open `http://localhost:8080/actuator/health`, or run:

```bash
curl http://localhost:8080/actuator/health
```

The response should contain `"status":"UP"`.

If startup reports that the database user does not exist, verify that the Docker database is running, that Spring Boot is reaching the intended port, and that its credentials match the initialized database.

## 7. Manage schema changes and shutdown

Add database changes as versioned SQL migrations under `backend/src/main/resources/db/migration/`, using names such as `V1__create_users.sql`. Flyway applies migrations at startup; Hibernate validates entity mappings. Add a new migration for changes to a schema already shared with the team.

Stop Spring Boot with `Ctrl+C`. Stop PostgreSQL from the repository root with:

```bash
docker compose down
```

This preserves the database volume. Adding `-v` deletes its data.
