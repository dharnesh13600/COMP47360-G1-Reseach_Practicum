#!/bin/bash
set -e

echo "Building and deploying Nginx reverse proxy..."

# Variables
PROJECT_ID="manhattan-muse"
REGION="europe-west2"
IMAGE_NAME="creative-space-nginx"

# Build Docker image
echo "Building Docker image..."
cd nginx/
docker build -t gcr.io/${PROJECT_ID}/${IMAGE_NAME}:latest .

# Push to Google Container Registry
echo "Pushing image to GCR..."
docker push gcr.io/${PROJECT_ID}/${IMAGE_NAME}:latest

# Deploy to Kubernetes
echo "Deploying to Kubernetes..."
cd ../k8s/

# Create namespace if it doesn't exist
kubectl create namespace creative-space-finder --dry-run=client -o yaml | kubectl apply -f -

# Apply Kubernetes manifests
kubectl apply -f nginx-configmap.yaml
kubectl apply -f nginx-deployment.yaml
kubectl apply -f nginx-service.yaml
kubectl apply -f nginx-hpa.yaml

echo "Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/nginx-reverse-proxy -n creative-space-finder

echo "Getting external IP..."
kubectl get service nginx-reverse-proxy-service -n creative-space-finder -o wide

echo "Nginx reverse proxy deployed successfully!"