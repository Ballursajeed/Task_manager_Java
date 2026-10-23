# Task Management REST API

A backend project built with Java 21, Spring Boot 4.1.2,
Spring Data JPA, and PostgreSQL 16.

## Features

- Create, list, fetch, update, and delete tasks.
- Generated UUID identifiers.
- Status and priority enums.
- Server-managed creation and update timestamps.
- Request validation and structured error responses.
- Separate controllers, services, repositories, DTOs, and entities.
- PostgreSQL persistence with Docker-based local setup.
- Six controller tests and one application-context startup test.

## Requirements

- JDK 21
- Docker
- Git

Maven is provided through the included Maven Wrapper.
Run the following commands from the project root using Bash.

## Environment variables

Set these in the terminal used to run or build the application:

```bash
export DB_USERNAME=taskuser
read -rsp "Database password: " DB_PASSWORD
echo
export DB_PASSWORD
export DB_URL=jdbc:postgresql://localhost:5433/taskmanager
```

Use the same password when reconnecting to an existing database.
Variables must be exported again in a new terminal.

## Start PostgreSQL

For the first run:

```bash
docker run -d \
  --name taskmanager-db \
  -e POSTGRES_DB=taskmanager \
  -e POSTGRES_USER="$DB_USERNAME" \
  -e POSTGRES_PASSWORD="$DB_PASSWORD" \
  -p 127.0.0.1:5433:5432 \
  -v taskmanager-data:/var/lib/postgresql/data \
  postgres:16
```

For subsequent runs, if the container is stopped:

```bash
docker start taskmanager-db
```

Check readiness:

```bash
docker exec taskmanager-db pg_isready -U taskuser -d taskmanager
```

The named Docker volume stores database data independently of the
container. PostgreSQL initialization settings apply only when the
data directory is empty.

## Run the application

```bash
./mvnw spring-boot:run
```

Base URL: http://localhost:8080

```bash
curl -i http://localhost:8080/health
```

## API endpoints

| Method | Endpoint | Success |
|---|---|---|
| POST | /api/tasks | 201 Created |
| GET | /api/tasks | 200 OK |
| GET | /api/tasks/{id} | 200 OK |
| PUT | /api/tasks/{id} | 200 OK |
| DELETE | /api/tasks/{id} | 204 No Content |

Missing tasks return 404.
Invalid request bodies, invalid enum values, validation failures,
and malformed UUIDs return 400.

The list endpoint returns tasks by creation time descending,
with ID descending as a tie-breaker.

## Request fields

| Field | Rules |
|---|---|
| title | Required, nonblank, maximum 150 characters |
| description | Optional, maximum 2,000 characters |
| status | TODO, IN_PROGRESS, DONE; defaults to TODO |
| priority | LOW, MEDIUM, HIGH; defaults to MEDIUM |

Omitted or null status/priority values use their defaults.

PUT replaces all editable fields. Title is required.
Omitted description becomes null; omitted status/priority reset
to their defaults. PUT does not create a missing task.

ID, createdAt, and updatedAt are server-managed.
An unchanged update may leave updatedAt unchanged.

## Example requests

Create:

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"Build a Spring Boot API","priority":"HIGH"}'
```

Copy the returned ID:

```bash
TASK_ID=replace-with-returned-uuid
```

List and fetch:

```bash
curl -i http://localhost:8080/api/tasks
curl -i "http://localhost:8080/api/tasks/$TASK_ID"
```

Update:

```bash
curl -i -X PUT "http://localhost:8080/api/tasks/$TASK_ID" \
  -H "Content-Type: application/json" \
  -d '{"title":"Build a Spring Boot API","status":"DONE","priority":"HIGH"}'
```

Delete:

```bash
curl -i -X DELETE "http://localhost:8080/api/tasks/$TASK_ID"
```

Validation example — returns 400:

```bash
curl -i -X POST http://localhost:8080/api/tasks \
  -H "Content-Type: application/json" \
  -d '{"title":"   "}'
```

## Tests and build

Controller tests without a database:

```bash
./mvnw -Dtest=TaskControllerTest test
```

Full test suite and executable JAR build:

```bash
./mvnw clean package
```

The full suite requires PostgreSQL and the exported environment
variables because the application-context test connects to the database.

Run the packaged application with the same environment variables:

```bash
java -jar target/taskmanager-0.0.1-SNAPSHOT.jar
```

Stop any other application instance using port 8080 first.

Controller tests use a mocked service; they do not verify database CRUD.
CRUD behaviour has also been checked manually using curl.

## Implementation notes

Controllers receive requests and validate request DTOs.
Services perform operations inside transactions.
Repositories use JPA/Hibernate to access PostgreSQL.
Entities are converted into response DTOs before returning JSON.

This learning project uses Hibernate ddl-auto=update for local schema
management. Production schema changes would use migrations.

Authentication, pagination, and filtering are outside the current scope.

## AI assistance

AI assisted with implementation code, explanations, and test examples.
I ran the application locally, checked endpoints using curl, inspected
the PostgreSQL schema, and ran the automated tests.