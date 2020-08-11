#!/bin/bash

docker exec court-case-service_postgres_1 /usr/local/bin/psql -d postgres -c 'DROP SCHEMA IF EXISTS courtcaseservice CASCADE; DROP SCHEMA IF EXISTS courtcaseservicetest CASCADE; CREATE SCHEMA courtcaseservice; CREATE SCHEMA courtcaseservicetest;'