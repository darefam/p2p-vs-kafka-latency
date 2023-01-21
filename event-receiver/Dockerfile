FROM openjdk:18
LABEL maintainer="dare@ultradovesystems.com"
ARG MODULE=event-receiver
ARG MODULE_BUILD_PATH=${MODULE}/build
WORKDIR /app
COPY ${MODULE_BUILD_PATH}/libs libs/
COPY ${MODULE_BUILD_PATH}/resources resources/
COPY ${MODULE_BUILD_PATH}/classes classes/
ENTRYPOINT ["java", "-Xms512m", "-Xmx512m", "-cp", "/app/resources:/app/classes:/app/libs/*", "com.meetup.kafkalondon.EventReceiverApplication"]
EXPOSE 8854