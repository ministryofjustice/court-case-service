#!/bin/bash

docker exec court-case-service_postgres_1 /usr/local/bin/psql -d postgres -c 'DROP SCHEMA courtcaseservice CASCADE; DROP SCHEMA courtcaseservicetest CASCADE; CREATE SCHEMA courtcaseservice; CREATE SCHEMA courtcaseservicetest;'