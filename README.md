Court Case Service
==================
Service to access court cases imported from HMCTS Libra court lists

Dev Setup
---
Requires Lombok installed in the IDE.

Build
---
```./gradlew build```

Run Locally
---
Start PostgreSQL instance

```docker-compose up```

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
