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
Start a local DynamoDB instance on `http://localhost:8000`:

```docker run -p 8000:8000 amazon/dynamodb-local```

Start the app under the `localDynamo` spring profile:

```java -Dspring.profiles.active=localDynamo -jar build/libs/court-case-service-{version}.jar```

Check dependency versions
---
```./gradlew dependencyUpdates```