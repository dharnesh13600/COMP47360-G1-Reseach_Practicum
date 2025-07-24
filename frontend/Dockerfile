FROM node:22-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm install
RUN npm install lucide-react

COPY . .
RUN npm run build

# Use a smaller image for the final stage

FROM node:22-alpine
# Set the working directory

WORKDIR /app

# Copy built files from the builder stage
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/node_modules ./node_modules

# Expose the port
EXPOSE 3000

# Start the app
CMD ["npm", "start"]