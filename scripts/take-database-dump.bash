origin_namespace=court-probation-dev
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


echo "🏗 Creating pg-dump-pod in destination namespace $destination_namespace..."
kubectl apply -f ./pg-dump-pod.yaml -n $destination_namespace

echo "⏱ Waiting for pod to come up"
sleep 10

echo "🔑 Getting credentials for origin namespace $origin_namespace..."
secret_json=$(kubectl get secret "court-case-service-rds-instance-output" -o json -n $origin_namespace | jq '.data | map_values(@base64d)')
database_name=$(echo "$secret_json" | jq -r .database_name)
database_username=$(echo "$secret_json" | jq -r .database_username)
database_password=$(echo "$secret_json" | jq -r .database_password)
rds_instance_address=$(echo "$secret_json" | jq -r .rds_instance_address)

echo "📡 Connecting to pg-dump-pod in $destination_namespace to take database dump..."

kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
pg_dump -U $database_username \
        -h $rds_instance_address \
        -d $database_name \
        -O \
        --schema=$schema_name \
        --section=pre-data \
        -f /data/pre-data.sql

echo "1️⃣ of 3️⃣ Pre-data dumped to pg-dump-pod:/data/pre-data.sql"

kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
pg_dump -U $database_username \
        -h $rds_instance_address \
        -d $database_name \
        -O \
        --schema=$schema_name \
        --section=data \
        -f /data/data.sql

echo "2️⃣ of 3️⃣ Data dumped to pg-dump-pod:/data/data.sql"

kubectl exec -ti pg-dump-pod -n $destination_namespace -- \
env PGPASSWORD=$database_password \
pg_dump -U $database_username \
        -h $rds_instance_address \
        -d $database_name \
        -O \
        --schema=$schema_name \
        --section=post-data \
        -f /data/post-data.sql

echo "3️⃣ of 3️⃣ Post-data dumped to pg-dump-pod:/data/post-data.sql"
