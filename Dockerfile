FROM java

COPY build/libs/court-list-service-*.jar /root/court-list-service.jar


ENTRYPOINT ["/usr/bin/java", "-jar", "/root/court-list-service.jar"]
