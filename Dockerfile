# Based on: https://github.com/UNIZAR-30246-WebEngineering/lab1-git-race/blob/master/Dockerfile

#Step 1: Compile
FROM gradle:6.6.1-jdk11 AS build
RUN mkdir /code
WORKDIR /code
COPY --chown=gradle:gradle . /code
RUN gradle build --no-daemon

#Step 2: Run image
FROM openjdk:11-jre-slim
EXPOSE 8080
RUN mkdir /app
WORKDIR /app
COPY --from=build /code/build/libs/UrlShortener.jar /app/UrlShortener.jar
ENTRYPOINT ["java"]
CMD ["-jar", "/app/UrlShortener.jar"]