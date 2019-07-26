FROM openjdk:11-slim

RUN addgroup --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

RUN mkdir -p /app
WORKDIR /app

COPY build/libs/court-list-service-*.jar /app/court-list-service.jar
COPY run.sh /app

RUN chown -R appuser:appgroup /app

USER 2000


ENTRYPOINT ["/bin/sh", "/app/run.sh"]