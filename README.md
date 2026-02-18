# Court Case Service
[![CircleCI](https://circleci.com/gh/ministryofjustice/court-case-service.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-case-service) 
[![Swagger API docs (needs VPN)](https://img.shields.io/badge/API_docs_(needs_VPN)-view-85EA2D.svg?logo=swagger)](https://court-case-service-dev.apps.live-1.cloud-platform.service.justice.gov.uk/swagger-ui.html#)

### Service to access court cases imported from HMCTS Libra and Common Platform court lists

For more information, check our [Runbook](https://dsdmoj.atlassian.net/wiki/spaces/NDSS/pages/2548662614/Prepare+a+Case+for+Sentence+RUNBOOK)

---

## Quick Start
This section contains the bare minimum you need to do to get the app running against the dev environment assuming you've got all the necessary dependencies (see Prerequisites section).
- Run `docker-compose up postgres` to start the postgres database Docker container
- Run `./gradlew clean build` to build the application
- To run against dev services (you will need to substitute in valid credentials, ask a maintainer for help obtaining these):
    - `SPRING_PROFILES_ACTIVE=dev`
    - `COMMUNITY_API_CLIENT_ID=<?>`
    - `COMMUNITY_API_CLIENT_SECRET=<?>`
    - `OFFENDER_ASSESSMENTS_API_CLIENT_ID=<?>`
    - `OFFENDER_ASSESSMENTS_API_CLIENT_SECRET=<?>`
  
  And then run`./gradlew bootRun`
- Application will now be [running on port 8080](http://localhost:8080/health)
- Optional: Run `./gradlew installGitHooks` to install Git hooks from `./hooks` directory. Note these require postgres to be running to pass.     
---
## Running Service Locally
Ensure all docker containers are up and running:

```bash
docker compose up -d
```

Which should start the following containers: (verify with `$ docker ps` if necessary)
- oauth
- court-case-service-postgres-1
- localstack-court-case-service
- community-api
- offender-assessment-api

Start the service ensuring the local spring boot profile is set:

`./gradlew bootRun --args='--spring.profiles.active=local'`

NB. All REST endpoints are secured with the role `PREPARE_A_CASE` which will need to be passed to the endpoint as an OAuth token.

### Running on Apple Silicon 
When running the service through IntelliJ you may see the following error in the server logs:

`Suppressed: java.lang.UnsatisfiedLinkError: no netty_resolver_dns_native_macos_aarch_64 in java.library.path:`

The service will still run in spite of this, and if need be this error can be mitigated with the followng addition in the `build.gradle` :
```
    implementation("io.netty:netty-resolver-dns-native-macos:4.1.75.Final") {
        artifact {
            classifier = "osx-aarch_64"
        }
    }
```
## Prerequisites
- Java 21
- Docker

We also use:
- `jenv` - If you have multiple versions of java locally
- `kubectl`,`minikube`,`helm` - For testing and managing k8s deployments
- `circleci` cli - For validating the circle configs
- `snyk` cli - For vulnerability checking


---

## API

The following actuator endpoints are available:
* *Application health* : `$ curl -X GET http://localhost:8080/health`
* *Application info* : `$ curl -X GET http://localhost:8080/info`
* *Application Ping* : `$ curl -X GET http://localhost:8080/ping`
* *Application Feature Flags* : `$ curl -X GET http://localhost:8080/feature-flags`

---

## Database
The application uses a Postgres 14 database which is managed by Flyway. The SpringBoot integration will automatically manage migrations, so we only need these commands for debugging or if the local database has become corrupted. 
* *Clean schema* : `$ ./gradlew flywayClean`
* *View details and status information about all migrations* : `$ ./gradlew flywayInfo`

> **Flyway clean safety**
>
> - `flywayClean` is guarded by the `cleanDisabled` flag in `build.gradle`. The task is disabled by default; you must explicitly set `FLYWAY_CLEAN_DISABLED=false` (or pass a `-P` flag that flips the property) before Gradle will execute it. This mirrors the application’s production stance where schema wipes are never allowed.
> - Only run this command against disposable local databases. **Never execute `./gradlew flywayClean` in production or any shared environment**; it drops every schema listed in the Flyway configuration and will permanently delete live data.

### Known issues
`ERROR: function uuid_generate_v4() does not exist`

Run `DROP EXTENSION "uuid-ossp";` in the database. This shouldn't happen given the `IF NOT EXISTS` in the offending migration but unfortunately there appears to be an issue with Postgres. It seems to be that this issue only occurs when the migration is run against a Postgres instance against which the migration has already been run at some point in the past - even if the schema has been subsequently deleted or if it was created under a different schema name. This is why it only usually happens in transient environments where schemas are often set up and torn down without destroying the underlying database.

---

## Testing

`docker compose up localstack-court-case-service postgres`
`./gradlew check`

### Linting
Run ktlint checks or auto-format:
```
./gradlew ktlintCheck
./gradlew ktlintFormat
```

### Git hooks
Install repo-provided hooks (includes pre-commit) into `.git/hooks`:
```
./gradlew installGitHooks
```

---

## Deployment

Builds and deployments are setup in `Circle CI` and configured in the config file.
Helm is used to deploy the service to a Kubernetes Cluster using templates in the `helm_deploy` folder.

---

## PACT

The service implements both consumer and provider contract tests using PACT. As a provider, it verifies the consumer PACTs generated by prepare-a-case and court-case-matcher. It also generates PACT contracts which define the interactions it expects as a consumer of the community-api.

### Consumer
To run build which generates and published the consumer PACTs to the broker, the following environment variables are required 

`PACTBROKER_AUTH_PASSWORD`
`PACTBROKER_AUTH_USERNAME`
`PACTBROKER_URL`

The PACTs can be generated and published, tagged with "main" with the following command

`PACTCONSUMER_VERSION=main ./gradlew -Dpact.writer.overwrite=true test pactPublish`

---

### Caching

The case list page is cacheable and returns a Last-Modified header for cache validation. There is an nginx docker container configured to do this in `./nginx/Dockerfile`.

To build and run the nginx cache against a local instance of court-case-service running on port 8090:

```
docker build ./nginx --tag court-case-service-proxy
docker run -p 8080:8080 --env SERVICE_HOST=http://host.docker.internal:8090 court-case-service-proxy
```

This will act as a simple reverse proxy with caching. It is configured to return an `X-Cache-Status` header which indicates whether the response was retrieved from the cache.

---

## Database Seeding

To populate test data locally, enable `db-seed` (config key `db-seed.enabled=true`).

Ensure your active Spring profile is one of `local`, `dev`, or `test` (guarded by `RouteAccessFilter`). 

Then call the REST endpoint:

```bash
curl -X POST "http://localhost:8080/db-seed" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '' \
  --get \
  --data-urlencode "count=10" \
  --data-urlencode "start=2026-02-13" \
  --data-urlencode "days=5" \
  --data-urlencode "court=B10JQ" \
  --data-urlencode "clean=true"
```

Parameters (all optional unless noted):
- `count` (int, default 1, min 1, max 500): number of cases to seed
- `start` (ISO date, default today): anchor date for hearing generation
- `days` (int, default 1, min 1, max 30): number of working days forward/backward
- `court` (string, default `B10JQ`): court code applied to generated hearings
- `clean` (boolean, default false): truncate seed-related tables before seeding

Notes:
- Endpoint is transactional; seeding and optional clean happen in one request.
- Clean is guarded by the `clean` flag on the request; the tables truncated are defined in `Seeder.clean()`.
- Only available when the feature flag and allowed profile conditions are met.
