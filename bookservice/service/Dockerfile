# syntax=docker/dockerfile:1

FROM openjdk:11 as env

WORKDIR /root/src/app
ENV SERVICE_DEPLOYMENT='service/bookservice_deploy.jar'

FROM env as dir_setup
COPY . .
RUN ls -la 

CMD java -jar ${SERVICE_DEPLOYMENT}