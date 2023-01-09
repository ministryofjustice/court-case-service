destination_namespace=court-probation-dev
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


echo "🔥 Deleting pg-dump-pod in destination namespace $destination_namespace..."
kubectl delete pod pg-dump-pod -n $destination_namespace
