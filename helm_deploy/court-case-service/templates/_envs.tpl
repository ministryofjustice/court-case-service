    {{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: "{{ .Values.image.port }}"

  - name: SPRING_PROFILES_ACTIVE
    value: "{{ .Values.spring.profile }}"

  - name: JAVA_OPTS
    value: "{{ .Values.env.JAVA_OPTS }}"

  - name: FEATURE_FLAGS_ENABLE_CACHEABLE_CASE_LIST
    value: "{{ .Values.env.FEATURE_FLAGS_ENABLE_CACHEABLE_CASE_LIST }}"

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

  - name: COMMUNITY_API_BASE_URL
    value: "{{ .Values.env.community_api.base_url }}"

  - name: NOMIS_OAUTH_BASE_URL
    value: "{{ .Values.env.nomis_oauth.base_url }}"

  - name: OFFENDER_ASSESSMENTS_API_BASE_URL
    value: "{{ .Values.env.offender_assessments_api.base_url }}"

  - name: PRISON_API_BASE_URL
    value: "{{ .Values.env.prison_api.base_url }}"

  - name: DELIUS_BASE_URL
    value: "{{ .Values.env.delius.base_url }}"

  - name: COURT_CASE_AND_DELIUS_API_BASE_URL
    value: "{{ .Values.env.court_case_and_delius_api.base_url }}"

  - name: MANAGE_OFFENCES_API_BASE_URL
    value: "{{ .Values.env.manage_offences_api.base_url }}"

  - name: DOMAIN_EVENT_AND_DELIUS_API_BASE_URL
    value: "{{ .Values.env.domain_event_and_delius_api.base_url }}"

  - name: HEARING_OUTCOMES_MOVE_UN_RESULTED_TO_OUTCOMES_CUTOFF_TIME
    value: "{{ .Values.env.hearing_outcomes.move_un_resulted_to_outcomes_cutoff_time }}"

  - name: HEARING_OUTCOMES_MOVE_UN_RESULTED_TO_OUTCOMES_COURTS
    value: "{{ .Values.env.hearing_outcomes.move_un_resulted_to_outcomes_courts }}"

  - name: HMPPS_DOCUMENT_MANAGEMENT_API_CLIENT_BASE_URL
    value: "{{ .Values.env.hmpps_document_management_api_client.base_url }}"

  - name: COURT_CASE_AND_DELIUS_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: COURT_CASE_AND_DELIUS_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: COMMUNITY_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: COMMUNITY_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: OFFENDER_ASSESSMENTS_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: OFFENDER_ASSESSMENTS_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: PRISON_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: PRISON_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: MANAGE_OFFENCES_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: MANAGE_OFFENCES_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: DOMAIN_EVENT_AND_DELIUS_API_CLIENT_ID
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: DOMAIN_EVENT_AND_DELIUS_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: HMPPS_DOCUMENT_MANAGEMENT_API_CLIENT
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-id

  - name: HMPPS_DOCUMENT_MANAGEMENT_API_CLIENT_SECRET
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: nomis-oauth-client-secret

  - name: APPINSIGHTS_INSTRUMENTATIONKEY
    valueFrom:
      secretKeyRef:
        name: court-case-service-secrets
        key: APPINSIGHTS_INSTRUMENTATIONKEY

  - name: HMPPS_SQS_TOPICS_HMPPSDOMAINEVENTS_ARN
    valueFrom:
      secretKeyRef:
        name: hmpps-domain-events-topic
        key: topic_arn

  - name: HMPPS_SQS_QUEUES_PICPROBATIONOFFENDEREVENTSQUEUE_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        name: probation-offender-events-court-case-service-main-queue
        key: sqs_queue_name

  - name: HMPPS_SQS_QUEUES_PICPROBATIONOFFENDEREVENTSQUEUE_DLQ_NAME
    valueFrom:
      secretKeyRef:
        name: probation-offender-events-court-case-service-dead-letter-queue
        key: sqs_queue_name

  - name: HMPPS_SQS_QUEUES_PICNEWOFFENDEREVENTSQUEUE_QUEUE_NAME
    valueFrom:
      secretKeyRef:
        name: sqs-pic-new-offender-events-secret
        key: sqs_queue_name

  - name: HMPPS_SQS_QUEUES_PICNEWOFFENDEREVENTSQUEUE_DLQ_NAME
    valueFrom:
      secretKeyRef:
        name: sqs-pic-new-offender-events-dlq-secret
        key: sqs_queue_name

  - name: DATABASE_READONLY_USERNAME
    valueFrom:
      secretKeyRef:
        name: court-case-service-readonly-user
        key: database_readonly_username

  - name: DATABASE_READONLY_PASSWORD
    valueFrom:
      secretKeyRef:
        name: court-case-service-readonly-user
        key: database_readonly_password

  {{- with (index .Values.ingress.hosts 0)}}
  - name: INGRESS_URL
    value: {{ .host }}
  {{- end }}
{{- end -}}
