FROM openjdk:21-slim AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER ${BUILD_NUMBER:-1_0_0}

WORKDIR /app
ADD . .
RUN ./gradlew assemble -Dorg.gradle.daemon=false

FROM eclipse-temurin:21.0.8_9-jre-jammy
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN groupadd --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Install AWS RDS Root cert into Java truststore
RUN mkdir -p /home/appuser/.postgresql
ADD --chown=appuser:appgroup https://truststore.pki.rds.amazonaws.com/eu-west-2/eu-west-2-bundle.pem /home/appuser/.postgresql/root.crt

WORKDIR /app

COPY --from=builder --chown=appuser:appgroup /app/build/libs/court-case-service*.jar /app/court-case-service.jar
COPY --from=builder --chown=appuser:appgroup /app/build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY --from=builder --chown=appuser:appgroup /app/applicationinsights.json /app
COPY --from=builder --chown=appuser:appgroup /app/run.sh /app

RUN mkdir -p /app/src/test/resources/db/migration
COPY src/test/resources/db/migration/local /app/src/test/resources/db/migration/local

RUN chown -R appuser:appgroup /app

USER 2000

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
