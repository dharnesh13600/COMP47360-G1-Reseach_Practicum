#!/bin/bash
set -e
echo "*** Docker Build and Push to Google Artifact Registry ***"

BASE_IMAGE_NAME="${AR_REGION}-docker.pkg.dev/${AR_PROJECT}/${AR_REPO_NAME}/backend"
UNIQUE_TAGGED_IMAGE="${BASE_IMAGE_NAME}:${IMAGE_TAG}"

echo "Building Docker image: ${UNIQUE_TAGGED_IMAGE}"

docker build -t "${UNIQUE_TAGGED_IMAGE}" -f backend/Dockerfile backend/

echo "Pushing Docker image with unique tag: ${UNIQUE_TAGGED_IMAGE}"

docker push "${UNIQUE_TAGGED_IMAGE}"

LATEST_TAGGED_IMAGE="${BASE_IMAGE_NAME}:latest
echo "Tagging image with :latest: ${LATEST_TAGGED_IMAGE}"

docker tag "${UNIQUE_TAGGED_IMAGE}" "${LATEST_TAGGED_IMAGE}"
echo "Pushing Docker image with latest tag: ${LATEST_TAGGED_IMAGE}"
docker push "${LATEST_TAGGED_IMAGE}"
echo "Docker operations completed successfully."