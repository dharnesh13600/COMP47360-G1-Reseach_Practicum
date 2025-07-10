#!/bin/sh
set -euo pipefail

# Required env vars:
: "${PROJECT_ID:?Need to set PROJECT_ID}"
: "${GCP_KEYFILE:?Need to set GCP_KEYFILE (path to service‐account JSON)}"
: "${REGION:?Need to set REGION (e.g. europe-west2)}"
: "${REPOSITORY:?Need to set REPOSITORY (Artifact Registry repo name)}"
: "${SERVICE:?Need to set SERVICE (image name, e.g. frontend/backend)}"
: "${IMAGE_TAG:?Need to set IMAGE_TAG}"

# Optional env vars (fallbacks shown):
DOCKERFILE="${DOCKERFILE:-Dockerfile}"
BUILD_CONTEXT="${CONTEXT:-.}"

echo "→ Authenticating to GCP as service account"
gcloud auth activate-service-account --key-file="$GCP_KEYFILE" --project="$PROJECT_ID"

echo "→ Configuring gcloud to use project $PROJECT_ID"
gcloud config set project "$PROJECT_ID"

echo "→ Enabling Docker credential helper for Artifact Registry"
gcloud auth configure-docker "${REGION}-docker.pkg.dev" --quiet

# Full image name in Artifact Registry
IMAGE_URI="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${SERVICE}:${IMAGE_TAG}"

echo "→ Building Docker image $IMAGE_URI"
docker build -f "$DOCKERFILE" "$BUILD_CONTEXT" -t "$IMAGE_URI"

echo "Pushing Docker image to Artifact Registry"
docker push "$IMAGE_URI"

echo "Successfully pushed $IMAGE_URI"
