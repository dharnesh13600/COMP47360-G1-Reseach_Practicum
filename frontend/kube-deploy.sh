#!/bin/sh

set -e

: "${PROJECT_ID:?PROJECT_ID must be set}"
: "${GCP_KEYFILE:?GCP_KEYFILE (path to JSON key) must be set}"
: "${GCP_ACCOUNTNAME:?GCP_ACCOUNTNAME must be set}"
: "${CLUSTER_NAME:?CLUSTER_NAME must be set}"
: "${LOCATION:?LOCATION (zone) must be set}"
: "${IMAGE:?IMAGE (full repo URI) must be set}"

#configure kubernetes access
echo "*** setting up kubernetes access based on service account token ***";

gcloud auth activate-service-account ${GCP_ACCOUNTNAME} --key-file=${GCRCREDFILE} --project=${PROJECT_ID}
gcloud container clusters get-credentials ${CLUSTER_NAME} --region=${LOCATION}

#kubernetes won't allow variables in the yaml files so using envsubst workaround so we can use them

echo "*** creating deployment yaml files ***";
env envsubst < k8s/frontend-deployment.tmpl > k8s/frontend-deployment.yaml;
env envsubst < k8s/frontend-service.tmpl > k8s/frontend-service.yaml;
env envsubst < k8s/ingress.tmpl > k8s/ingress.yaml;
echo "*** deploying docker container and setting up the service and ingress  ***";

#cat frontend-deployment.yaml;
#cat frontend-service.yaml;

# kubectl delete -f k8s/frontend-deployment.yaml --validate=false --insecure-skip-tls-verify=true;

kubectl apply -f k8s/frontend-deployment.yaml --validate=false --insecure-skip-tls-verify=true;
kubectl apply -f k8s/frontend-service.yaml --validate=false --insecure-skip-tls-verify=true;
kubectl apply -f k8s/ingress.yaml --validate=false --insecure-skip-tls-verify=true;

# kubectl apply -f k8s/secret.yaml --validate=false --insecure-skip-tls-verify=true;
