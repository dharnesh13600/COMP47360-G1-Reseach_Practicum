# #!/bin/sh
# set -euo pipefail

# Required env vars:
# : "${PROJECT_ID:?Need to set PROJECT_ID}"
# : "${GCP_KEYFILE:?Need to set GCP_KEYFILE (path to service‐account JSON)}"
# : "${REGION:?Need to set REGION (e.g. europe-west2)}"
# : "${REPOSITORY:?Need to set REPOSITORY (Artifact Registry repo name)}"
# : "${SERVICE:?Need to set SERVICE (image name, e.g. frontend/backend)}"
# : "${IMAGE_TAG:?Need to set IMAGE_TAG}"

# # Optional env vars (fallbacks shown):
# DOCKERFILE="${DOCKERFILE:-Dockerfile}"
# BUILD_CONTEXT="${CONTEXT:-.}"

# echo "→ Authenticating to GCP as service account"
# gcloud auth activate-service-account --key-file="$GCP_KEYFILE" --project="$PROJECT_ID"

# echo "→ Configuring gcloud to use project $PROJECT_ID"
# gcloud config set project "$PROJECT_ID"

# echo "→ Enabling Docker credential helper for Artifact Registry"
# gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# # Full image name in Artifact Registry
# IMAGE_URI="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${SERVICE}:${IMAGE_TAG}"

# echo "→ Building Docker image $IMAGE_URI"
# docker build -f "$DOCKERFILE" "$BUILD_CONTEXT" -t "$IMAGE_URI"

# echo "Pushing Docker image to Artifact Registry"
# docker push "$IMAGE_URI"

# echo "Successfully pushed $IMAGE_URI"

#----------------------------------------------------------------------------

#!/bin/sh
set -euo pipefail

# set -e
echo "*** Docker Build and Push to Google Artifact Registry ***"

: "${PROJECT_ID:?Need to set PROJECT_ID}"
: "${GCP_KEYFILE:?Need to set GCP_KEYFILE (path to service‐account JSON)}"
: "${REGION:?Need to set REGION (e.g. europe-west2)}"
: "${REPOSITORY:?Need to set REPOSITORY (Artifact Registry repo name)}"
: "${SERVICE:?Need to set SERVICE (image name, e.g. frontend/backend)}"
: "${IMAGE_TAG:?Need to set IMAGE_TAG}"

# Define the base image name without any tag
BASE_IMAGE_NAME="${AR_REGION}-docker.pkg.dev/${AR_PROJECT}/${AR_REPO_NAME}/backend"

# Define the full image name with the unique build tag (e.g., main-29)
UNIQUE_TAGGED_IMAGE="${BASE_IMAGE_NAME}:${IMAGE_TAG}"

echo "Building Docker image: ${UNIQUE_TAGGED_IMAGE}"
# This command uses your Dockerfile to build the image.
# The Dockerfile itself should be in backend/Dockerfile
docker build -t "${UNIQUE_TAGGED_IMAGE}" -f backend/Dockerfile backend/

echo "Pushing Docker image with unique tag: ${UNIQUE_TAGGED_IMAGE}"
docker push "${UNIQUE_TAGGED_IMAGE}"

# Define the full image name with the 'latest' tag
LATEST_TAGGED_IMAGE="${BASE_IMAGE_NAME}:latest"

echo "Tagging image with :latest: ${LATEST_TAGGED_IMAGE}"
# Tag the uniquely built image with the 'latest' tag
docker tag "${UNIQUE_TAGGED_IMAGE}" "${LATEST_TAGGED_IMAGE}"

echo "Pushing Docker image with latest tag: ${LATEST_TAGGED_IMAGE}"
# Push the 'latest' tagged image to Artifact Registry
docker push "${LATEST_TAGGED_IMAGE}"

echo "Docker operations completed successfully."

