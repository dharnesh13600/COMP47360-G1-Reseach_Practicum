#!/bin/sh
set -euo pipefail

echo "*** Setting up Kubernetes access based on service account token ***"

GCP_KEYFILE_PATH="$1"

echo "DEBUG: GCP_KEYFILE_PATH=${GCP_KEYFILE_PATH}"
test -r "${GCP_KEYFILE_PATH}" || { echo "Key not readable"; exit 1; }

echo "DEBUG: IMAGE_URL=${IMAGE_URL}"

gcloud auth activate-service-account --key-file="${GCP_KEYFILE_PATH}" --project="${PROJECT_ID}"
gcloud container clusters get-credentials "${CLUSTER_NAME}" --zone "${LOCATION}"

echo "*** Creating deployment YAML files using envsubst ***"
mkdir -p output

env IMAGE_URL="${IMAGE_URL}" envsubst < k8s/backend-deployment.tmpl > output/backend-deployment.yaml
env envsubst < k8s/backend-service.tmpl > output/backend-service.yaml
# render ingress too (if this pipeline owns it)
env envsubst < k8s/ingress.tmpl > output/ingress.yaml

echo "*** Deploying ***"
kubectl apply -f output/backend-deployment.yaml
kubectl apply -f output/backend-service.yaml
kubectl apply -f output/ingress.yaml   # comment out if frontend job owns the ingress

echo "Deployment commands executed successfully."
