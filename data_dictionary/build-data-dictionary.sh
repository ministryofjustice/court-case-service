#!/bin/sh -e
basedir=$(dirname "$0")
jardir="$basedir/libs"
schemadir="$basedir/schema"
pgdatabase=${POSTGRES_DB:-court_case_test}

mkdir -p "$jardir" "$schemadir"

./gradlew flywayMigrate -Pflyway.url="jdbc:postgresql://localhost:5432/$pgdatabase" \
  -Dflyway.user=root \
  -Dflyway.password=dev \
  -Dflyway.locations="classpath:db/migration/courtcase"

wget https://github.com/schemaspy/schemaspy/releases/download/v6.1.0/schemaspy-6.1.0.jar -O "$jardir/schemaspy.jar"
wget https://jdbc.postgresql.org/download/postgresql-42.2.19.jar -O "$jardir/postgresql.jar"

java -jar "$jardir/schemaspy.jar" -t pgsql -dp "$jardir/postgresql.jar" \
  -db "$pgdatabase" -host "localhost" -port "5432" -u "root" -p "dev" \
  -nopages \
  -o "${schemadir}"

ls -lrth "${schemadir}"
echo "current direcotory: ${schemadir}" && pwd
