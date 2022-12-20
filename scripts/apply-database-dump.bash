destination_namespace=court-probation-dev
schema_name=courtcaseservice
set -e
# Read any named params
while [ $# -gt 0 ]; do

   if [[ $1 == *"--"* ]]; then
        param="${1/--/}"
        declare $param="$2"
   fi

  shift
done

if [[ "$destination_namespace" = *-prod ]]; then
  echo "❌  Nope! -- This script should not be applied with a prod destination_namespace of $destination_namespace. This was probably a mistake but if you want to restore a prod instance use an RDS snapshot instead. Exiting 👋"
  exit 1
fi

echo "🔑 Getting credentials for origin namespace $destination_namespace..."
secret_json=$(kubectl get secret "court-case-service-rds-instance-output" -o json -n $destination_namespace | jq '.data | map_values(@base64d)')
database_name=$(echo "$secret_json" | jq -r .database_name)
database_username=$(echo "$secret_json" | jq -r .database_username)
database_password=$(echo "$secret_json" | jq -r .database_password)
rds_instance_address=$(echo "$secret_json" | jq -r .rds_instance_address)

echo "📡 Connecting to pg-dump-pod in $destination_namespace to apply database dump..."

backup_name=${schema_name}_bk_$(date | sed -e 's/ /_/g'| sed -e 's/:/_/g')

echo "💾 Renaming existing schema from $schema_name to $backup_name"
kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
psql -U $database_username -h $rds_instance_address -d $database_name \
-c "ALTER SCHEMA $schema_name RENAME TO $backup_name;"

echo "🗃 Applying dumped data..."

echo "1️⃣ of 3️⃣ Applying pre-data from pg-dump-pod:/data/pre-data.sql"
kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
psql -U $database_username \
     -h $rds_instance_address \
     -d $database_name \
     -f /data/pre-data.sql

echo "2️⃣ of 3️⃣ Applying data from pg-dump-pod:/data/data.sql"
kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
psql -U $database_username \
     -h $rds_instance_address \
     -d $database_name \
     -f /data/data.sql


echo "3️⃣ of 3️⃣ Applying post-data from pg-dump-pod:/data/post-data.sql"
kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
psql -U $database_username \
     -h $rds_instance_address \
     -d $database_name \
     -f /data/post-data.sql


