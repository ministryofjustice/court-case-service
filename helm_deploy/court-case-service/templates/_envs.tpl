    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "postgres,logstash"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: DATABASE_USERNAME
    valueFrom:
      secretKeyRef:
        name: court-case-service-rds-instance-output
        key: database_username

  - name: DATABASE_PASSWORD
    valueFrom:
      secretKeyRef:
        name: court-case-service-rds-instance-output
        key: database_password

  - name: DATABASE_NAME
    valueFrom:
      secretKeyRef:
        name: court-case-service-rds-instance-output
        key: database_name

  - name: DATABASE_ENDPOINT
    valueFrom:
      secretKeyRef:
        name: court-case-service-rds-instance-output
        key: rds_instance_endpoint

  - name: NOMIS_OAUTH_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: NOMIS_OAUTH_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret
{{- end -}}
