namespace=court-probation-dev
values=values-dev
set -e
# Read any named params
while [ $# -gt 0 ]; do

   if [[ $1 == *"--"* ]]; then
        param="${1/--/}"
        declare $param="$2"
   fi

  shift
done


if [[ "$namespace" = *-prod ]]; then
  echo "❌  Nope! -- This script should not be applied with a prod namespace of $namespace, use CircleCI for prod deployments. Exiting 👋"
  exit 1
fi

cd ../
helm upgrade court-case-service ./helm_deploy/court-case-service --values ./helm_deploy/${values}.yaml --install --namespace $namespace
