FROM gradle:jdk11-alpine as build

# God forgive me for this 1.5GB image but I just want to see if it works

WORKDIR /app

COPY . /app

# Build the JAR file
RUN gradle jar

# Install Chromedriver
RUN apk add --no-cache chromium chromium-chromedriver

# Set environment variables for Chromedriver
ENV CHROME_BIN=/usr/bin/chromium-browser
ENV CHROME_DRIVER=/usr/bin/chromedriver

# Set up a non-root user
USER root
RUN adduser -D regular_everyday_normal_mfuka
RUN chown -R regular_everyday_normal_mfuka /app
USER regular_everyday_normal_mfuka


ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=80"
CMD java -jar /app/build/libs/klite-klite.jar

# Heroku redefines exposed port
ENV PORT=8080
EXPOSE $PORT
