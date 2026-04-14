FROM node:20-alpine

WORKDIR /usr/src/app

COPY package*.json ./

RUN npm ci --omit=dev

COPY src ./src
COPY .env.example ./.env.example

EXPOSE 8083

CMD ["node", "src/server.js"]
