set -e
echo "*** Setting up Kubernetes access based on service account token ***"

# Accept the key file path as the first argument
GCP_KEYFILE_PATH="$1"

# Add debug inside the script to see what $GCP_KEYFILE_PATH is
echo "DEBUG (in kube-deploy.sh): GCP_KEYFILE_PATH is: ${GCP_KEYFILE_PATH}"
test -f "${GCP_KEYFILE_PATH}" && echo "DEBUG (in kube-deploy.sh): Key file exists and is a regular file." || echo "DEBUG (in kube-deploy.sh): Key file does NOT exist or is not a regular file."
test -r "${GCP_KEYFILE_PATH}" && echo "DEBUG (in kube-deploy.sh): Key file is readable." || echo "DEBUG (in kube-deploy.sh): Key file is NOT readable."

# Add debug for IMAGE_URL (this will tell us if it's coming through correctly)
echo "DEBUG (in kube-deploy.sh): IMAGE_URL is: ${IMAGE_URL}"

# Authenticate with gcloud service account
gcloud auth activate-service-account --key-file="${GCP_KEYFILE_PATH}" --project="${PROJECT_ID}"

# Get cluster credentials
gcloud container clusters get-credentials "${CLUSTER_NAME}" --zone="${LOCATION}"

echo "*** Creating deployment YAML files using envsubst ***"
mkdir -p "./output"

# CORRECTED LINE: Output filename changed from backend-deployment.yaml to backend_deployment.yaml
env IMAGE_URL="${IMAGE_URL}" PROJECT_ID="${PROJECT_ID}" CLUSTER_NAME="${CLUSTER_NAME}" LOCATION="${LOCATION}" \
envsubst < "k8s/backend_deployment.tmpl" > "./output/backend_deployment.yaml"

# Assuming you also want to name the service file consistently:
env IMAGE_URL="${IMAGE_URL}" \
envsubst < "k8s/backend_service.tmpl" > "./output/backend_service.yaml"

# Assuming ingress.tmpl is also directly under k8s/ and the output name is ingress.yaml
# If ingress.tmpl is not present in your k8s folder as per the screenshot, you might remove this line
# env IMAGE_URL="${IMAGE_URL}" \
# envsubst < "k8s/ingress.tmpl" > "./output/ingress.yaml"


echo "*** Deploying Docker container and setting up the service and ingress ***"
# Note: --validate=false and --insecure-skip-tls-verify=true are not recommended for production
# kubectl apply -f "./output/backend_deployment.yaml" --validate=false --insecure-skip-tls-verify=true
# kubectl apply -f "./output/backend_service.yaml" --validate=false --insecure-skip-tls-verify=true
kubectl apply -f "./output/backend_deployment.yaml"
kubectl apply -f "./output/backend_service.yaml"

# kubectl apply -f "./output/ingress.yaml" --validate=false --insecure-skip-tls-verify=true

echo "Deployment commands executed successfully."
