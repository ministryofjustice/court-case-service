# Court Case Service
[![CircleCI](https://circleci.com/gh/ministryofjustice/court-case-service.svg?style=svg)](https://circleci.com/gh/ministryofjustice/court-case-service) 
[![Swagger API docs (needs VPN)](https://img.shields.io/badge/API_docs_(needs_VPN)-view-85EA2D.svg?logo=swagger)](https://court-case-service-dev.apps.live-1.cloud-platform.service.justice.gov.uk/swagger-ui.html#)

### Service to access court cases imported from HMCTS Libra court lists

---


## Quick Start
This section contains the bare minimum you need to do to get the app running against the dev environment assuming you've got all the necessary dependencies (see Prerequisites section).
- Run `docker-compose up postgres` to start the postgres database Docker container
- Run `./gradlew clean build` to build the application
- To run against dev services (you will need to substitute in valid credentials, ask a maintainer for help obtaining these):
    - `env SPRING_PROFILES_ACTIVE=dev \
       COMMUNITY_API_CLIENT_ID=<?> \
       COMMUNITY_API_CLIENT_SECRET=<?> \
       OFFENDER_ASSESSMENTS_API_CLIENT_ID=<?> \
       OFFENDER_ASSESSMENTS_API_CLIENT_SECRET=<?> \
       ./gradlew bootRun`
- Application will now be [running on port 8080](http://localhost:8080/health)
     
---
       
## Prerequisites
- Java 14
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
The application uses a Postgres 11 database which is managed by Flyway. The SpringBoot integration will automatically manage migrations, so we only need these commands for debugging or if the local database has become corrupted. 
* *Clean schema* : `$ ./gradlew flywayClean`
* *View details and status information about all migrations* : `$ ./gradlew flywayInfo`

---

## Deployment

Builds and deployments are setup in `Circle CI` and configured in the config file.
Helm is used to deploy the service to a Kubernetes Cluster using templates in the `helm_deploy` folder.

