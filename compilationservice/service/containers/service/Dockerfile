# syntax=docker/dockerfile:1

FROM openjdk:11 as env

WORKDIR /root/src/app
ARG DEPLOYMENT_TARGET='compilationservice_deploy.jar'
ENV SERVICE_DEPLOYMENT=${DEPLOYMENT_TARGET}

FROM env as dir_setup
COPY . .
RUN ls -la 

CMD java -jar ${SERVICE_DEPLOYMENT}