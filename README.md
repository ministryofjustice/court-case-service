Court Case Service
==================
Service to access court cases imported from HMCTS Libra court lists

Dev Setup
---

In order to run the service locally, a postgres database and some other backend services are required, the easiest way to run locally is using the [docker-compose.yml](docker-compose.yml) file which will pull down the latest version.

```docker-compose up```

In order for the authentication to function between services locally you must add the following line to your `/etc/hosts` file before starting:

```
127.0.0.1 oauth
```

The service uses Lombok and so annotation processors must be [turned on within the IDE](https://www.baeldung.com/lombok-ide).

Building and running
---

This service is built using Gradle. In order to build the project from the command line, run

```./gradlew build```

To run the service, ensure there is an instance of Postgres running and then run

```SPRING_PROFILES_ACTIVE=local ./gradlew bootRun```

Dependencies
---
The service has an attached Postgres database as well as several back ends the details of which can be found in the [docker-compose.yml](docker-compose.yml) file.

To run against local Dockerised back-ends and database

```docker-compose up```

Once the database container is running, initialise the application database schemas

```initSchema.sh```

There are also Wiremock stubs for each of the back end calls which the `test` Spring profile runs against, to run these use the following command along with `docker-compose up`

```bash runMocks.sh```

### Default port
Starts the application on port '8080'.
To override, set server.port (eg SERVER_PORT=8099 java -jar etc etc)

### Swagger UI
The project builds swagger specifications which can be examined and tested via the Swagger UI.

http://localhost:8080/swagger-ui.html

### Application health
```
curl -X GET http://localhost:8080/health
```

### Application info
```
curl -X GET http://localhost:8080/info
```

### Application Ping
```
curl -X GET http://localhost:8080/ping
```

### Application Feature Flags
```
curl -X GET http://localhost:8080/feature-flags
```

Flyway commands
---

Migrate database

```gradle flywayMigrate -i```

View details and status information about all migrations

```gradle flywayInfo```

Baseline an existing database, excluding all migrations up to and including baselineVersion

```gradle flywayBaseline```

Check dependency versions
---
```./gradlew dependencyUpdates```

## Deployment

Builds and deployments are setup in [Circle CI](https://circleci.com/gh/ministryofjustice/court-case-service) and configured in the [config file.](.circleci/config.yml)

Helm is used to deploy the service to a Kubernetes Cluster using templates in the helm_deploy folder.
