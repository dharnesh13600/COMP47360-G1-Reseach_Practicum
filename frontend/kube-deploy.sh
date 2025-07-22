#!/bin/sh

set -e

: "${GCP_PROJECT:?GCP_PROJECT must be set}"
: "${GCP_KEYFILE:?GCP_KEYFILE (path to JSON key) must be set}"
: "${GCP_ACCOUNTNAME:?GCP_ACCOUNTNAME must be set}"
: "${CLUSTER_NAME:?CLUSTER_NAME must be set}"
: "${LOCATION:?LOCATION (zone) must be set}"
: "${IMAGE_URI:?IMAGE_URI (full repo URI) must be set}"

GCP_KEYFILE_PATH="$1"

echo "DEBUG (in kube-deploy.sh): GCP_KEYFILE_PATH is: ${GCP_KEYFILE_PATH}"
test -f "${GCP_KEYFILE_PATH}" && echo "DEBUG (in kube-deploy.sh): Key file exists and is a regular file." || echo "DEBUG (in kube-deploy.sh): Key file does NOT exist or is not a regular file."
test -r "${GCP_KEYFILE_PATH}" && echo "DEBUG (in kube-deploy.sh): Key file is readable." || echo "DEBUG (in kube-deploy.sh): Key file is NOT readable."

#configure kubernetes access
echo "*** setting up kubernetes access based on service account token ***";

# gcloud auth activate-service-account --key-file=${GCP_KEYFILE} --project=${GCP_PROJECT}
gcloud auth activate-service-account --key-file="${GCP_KEYFILE_PATH}" --project="${PROJECT_ID}"
# gcloud container clusters get-credentials ${CLUSTER_NAME} --zone=${LOCATION}
gcloud container clusters get-credentials "${CLUSTER_NAME}" --zone="${LOCATION}"


#kubernetes won't allow variables in the yaml files so using envsubst workaround so we can use them

echo "*** creating deployment yaml files ***";
envsubst < k8s/frontend-deployment.tmpl > k8s/frontend-deployment.yaml;
envsubst < k8s/frontend-service.tmpl > k8s/frontend-service.yaml;
envsubst < k8s/ingress.tmpl > k8s/ingress.yaml;
echo "*** deploying docker container and setting up the service and ingress  ***";

#cat frontend-deployment.yaml;
#cat frontend-service.yaml;

# kubectl delete -f k8s/frontend-deployment.yaml --validate=false --insecure-skip-tls-verify=true;

kubectl apply -f k8s/frontend-deployment.yaml --validate=false --insecure-skip-tls-verify=true;
kubectl apply -f k8s/frontend-service.yaml --validate=false --insecure-skip-tls-verify=true;
kubectl apply -f k8s/ingress.yaml --validate=false --insecure-skip-tls-verify=true;

# kubectl apply -f k8s/secret.yaml --validate=false --insecure-skip-tls-verify=true;
