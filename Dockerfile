# build container
FROM gradle:jdk21 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon -x test

# server container
FROM openjdk:21
WORKDIR /usr/src/task-manager
COPY --from=build /home/gradle/src/build/libs/*.jar ./task-manager.jar
CMD ["java", "-jar", "-Dlogging.level.root=DEBUG", "-Dlogging.level.org.springframework=DEBUG", "task-manager.jar"]