#!/bin/bash

docker exec court-case-service_postgres_1 /usr/local/bin/psql -d postgres -c 'CREATE SCHEMA courtcaseservice; CREATE SCHEMA courtcaseservicetest;'