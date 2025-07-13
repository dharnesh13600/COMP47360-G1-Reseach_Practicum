# FROM node:22-alpine AS builder
# WORKDIR /app

# COPY package*.json ./
# RUN npm install
# RUN npm install react-icons

# COPY . .
# RUN npm run build

# # Use a smaller image for the final stage

# FROM node:22-alpine
# # Set the working directory

# WORKDIR /app

# # Copy built files from the builder stage
# COPY --from=builder /app/.next ./.next
# COPY --from=builder /app/public ./public
# COPY --from=builder /app/package*.json ./
# COPY --from=builder /app/node_modules ./node_modules

# # Expose the port
# EXPOSE 3000

# # Start the app
# CMD ["npm", "start"]

# Stage 1

FROM node:22-alpine AS builder

WORKDIR /app

COPY package*.json ./
RUN npm ci --no-audit --prefer-offline     # deterministic & fast layer

COPY . .
RUN npm run build

# Stage 2

FROM node:22-alpine

WORKDIR /app
ENV NODE_ENV=production

# 3. install *production* deps only
COPY package*.json ./
RUN npm ci --omit=dev --no-audit --prefer-offline

# 4. copy the compiled output & public assets
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# 5. expose port & boot
EXPOSE 3000
CMD ["npm", "start"]
