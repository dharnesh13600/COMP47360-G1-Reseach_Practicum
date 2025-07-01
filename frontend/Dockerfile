FROM node:18-alpine

RUN npm install -g serve

WORKDIR /usr/src/app

COPY package*.json ./
RUN npm ci --production=false

COPY . .
RUN npm run build && npm run export

EXPOSE 80
CMD ["serve", "-s", "out", "-l", "80"]
