FROM openjdk:19-jdk-slim-buster
MAINTAINER HMPPS Digital Studio <info@digital.justice.gov.uk>

RUN yum clean all && yum update -y
RUN yum install -y curl
RUN yum -y update expat
ENV TZ=Europe/London
RUN ln -snf "/usr/share/zoneinfo/$TZ" /etc/localtime && echo "$TZ" > /etc/timezone

RUN groupadd --gid 2000 --system appgroup && \
    adduser --uid 2000 --system appuser --gid 2000

# Install AWS RDS Root cert into Java truststore
RUN mkdir /home/appuser
RUN mkdir /home/appuser/.postgresql \
  && curl https://s3.amazonaws.com/rds-downloads/rds-ca-2019-root.pem \
    > /home/appuser/.postgresql/root.crt
RUN curl https://s3.amazonaws.com/rds-downloads/rds-ca-2015-root.pem \
    >> /home/appuser/.postgresql/root.crt

WORKDIR /app

COPY build/libs/court-case-service*.jar /app/court-case-service.jar
COPY build/libs/applicationinsights-agent*.jar /app/agent.jar
COPY applicationinsights.json /app
COPY run.sh /app
RUN chown -R appuser:appgroup /app

USER 2000

ENTRYPOINT ["/bin/sh", "/app/run.sh"]
