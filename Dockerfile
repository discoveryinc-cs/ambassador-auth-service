# This stage installs our modules
# https://hub.docker.com/r/mhart/alpine-node
FROM mhart/alpine-node:12
WORKDIR /app
COPY package.json package-lock.json ./

# If you have native dependencies, you'll need extra tools
# RUN apk add --no-cache make gcc g++ python

#RUN apk add --no-cache --virtual .gyp python make g++ \
#    && npm install [ your npm dependencies here ] \
#    && apk del .gyp

RUN npm ci --prod

# Then we copy over the modules from above onto a `slim` image
FROM mhart/alpine-node:slim-12

LABEL PROJECT_REPO_URL         = "git@github.com:grengojbo/ambassador-auth-service.git" \
      PROJECT_REPO_BROWSER_URL = "https://github.com/grengojbo/ambassador-auth-service" \
      DESCRIPTION              = "Example auth service for Ambassador" \
      VENDOR                   = "Datawire, Inc." \
      VENDOR_URL               = "https://datawire.io/"

WORKDIR /app
EXPOSE 3000

COPY --from=0 /app .
COPY . .

RUN mkdir -p /vault/secrets
RUN mv /app/authkey.json /vault/secrets/

CMD ["node", "server.js"]

