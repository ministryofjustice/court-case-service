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

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: DYNAMO_ACCESS_KEY
    valueFrom:
      secretKeyRef:
        name: court-case-dynamodb-output
        key: access_key_id

  - name: DYNAMO_SECRET_KEY
    valueFrom:
      secretKeyRef:
        name: court-case-dynamodb-output
        key: secret_access_key

  - name: DYNAMO_TABLE
    valueFrom:
      secretKeyRef:
        name: court-case-dynamodb-output
        key: table_name

{{- end -}}
