FROM nginx:1.25-alpine

# Install all basic tools for debugging
RUN apk add --no-cache curl

# Copy the nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create a log directory
RUN mkdir -p /var/log/nginx

# Set the proper permissions
RUN chown -R nginx:nginx /var/log/nginx

# Health check for proxy
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost/nginx-health || exit 1

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]