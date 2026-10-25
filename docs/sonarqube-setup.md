# SonarQube Setup and Analysis Guide

This guide details how to run SonarQube and analyze both the **Spring Boot backend** and **Next.js frontend** using Docker Compose.

---

## Architecture Overview

```
                      +-----------------------------+
                      |       Docker Compose        |
                      +-----------------------------+
                                     |
           +-------------------------+-------------------------+
           |                         |                         |
+--------------------+      +------------------+      +--------------------+
|    sonarqube-db    |      |    sonarqube     |      |   sonar-analyzer   |
| (PostgreSQL 17-alp)|<-----| (LTS Community)  |<-----| (JDK21+Maven+Node  |
+--------------------+      |     Port 9000    |      |  +SonarScannerCLI) |
                            +------------------+      +--------------------+
                                     ^                         |
                                     |--- Backend Scan --------|
                                     |--- Frontend Scan -------|
```

The setup includes:
1. **`sonarqube`**: SonarQube LTS Community server exposed at `http://localhost:9000`.
2. **`sonarqube-db`**: Dedicated PostgreSQL database for SonarQube with persistent storage.
3. **`sonar-analyzer`**: A containerized analyzer with Java 21, Maven, Node.js, and SonarScanner CLI that mounts the repository, waits for SonarQube to be ready, builds backend bytecode, and executes the static code analysis for both backend and frontend.
4. **`backend/Dockerfile`**: Multi-stage production container for Spring Boot (Eclipse Temurin 21).
5. **`frontend/Dockerfile`**: Multi-stage production container for Next.js (Node 22).

---

## Quick Start (Automated Analysis)

To start SonarQube and run the analysis with one command:

```bash
./run-sonar-analysis.sh
```

### Selective Analysis Modes

```bash
# Analyze only backend
./run-sonar-analysis.sh --backend

# Analyze only frontend
./run-sonar-analysis.sh --frontend

# Analyze the entire repository as a unified project
./run-sonar-analysis.sh --monorepo

# Stop SonarQube services
./run-sonar-analysis.sh --down
```

---

## Running with Docker Compose Directly

### 1. Start SonarQube Server

```bash
docker compose up -d sonarqube
```

SonarQube takes about 30–60 seconds on initial startup to initialize its database and plugins. You can monitor the startup logs with:

```bash
docker compose logs -f sonarqube
```

Once the health check passes (`docker compose ps` shows `healthy`), open `http://localhost:9000`. Default credentials are `admin` / `admin`.

### 2. Trigger the Analyzer Service

Run the analyzer container using Docker Compose:

```bash
# Analyzes both backend and frontend
docker compose run --rm sonar-analyzer

# Or analyze only backend
docker compose run --rm -e SCAN_MODE=backend sonar-analyzer

# Or analyze only frontend
docker compose run --rm -e SCAN_MODE=frontend sonar-analyzer
```

---

## Running the Entire Application Stack

To spin up the entire application along with SonarQube:

```bash
docker compose up -d
```

This starts:
- `db` (Application PostgreSQL)
- `backend` (Spring Boot API at `http://localhost:8080`)
- `frontend` (Next.js at `http://localhost:3000`)
- `sonarqube-db` (SonarQube PostgreSQL)
- `sonarqube` (SonarQube Server at `http://localhost:9000`)

To stop all services:

```bash
docker compose down
```

Add `-v` if you wish to remove database and Sonar volumes.

---

## Running Analysis Outside Docker (Locally)

If you have JDK 21, Maven, and Node.js installed locally and SonarQube running at `http://localhost:9000`:

### Backend:
```bash
cd backend
./mvnw clean test-compile sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

### Frontend:
```bash
cd frontend
npx sonarqube-scanner \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=admin \
  -Dsonar.password=admin
```

---

## SonarQube Project Dashboards

Once analysis completes, inspect the results at:
- **Backend Quality Gate & Issues**: `http://localhost:9000/dashboard?id=marketplace-backend`
- **Frontend Quality Gate & Issues**: `http://localhost:9000/dashboard?id=marketplace-frontend`
- **Unified Monorepo (if scanned with `--monorepo`)**: `http://localhost:9000/dashboard?id=marketplace`
