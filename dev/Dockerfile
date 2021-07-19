FROM openjdk:11-jdk

RUN mkdir -p /tmp/build/src
COPY ./src/ /tmp/build/src/
RUN mkdir -p /tmp/build/gradle
COPY ./gradle/ /tmp/build/gradle/
COPY ./build.gradle.kts /tmp/build
COPY ./gradlew /tmp/build
COPY ./settings.gradle.kts /tmp/build
RUN mkdir /opt/users-app \
  && /tmp/build/gradlew -p /tmp/build/ -g /tmp/build/ -i bootJar \
  && cp /tmp/build/build/libs/users-1.0.0-SNAPSHOT.jar /opt/users-app/ \
  && rm -rf /tmp/build/
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "/opt/users-app/users-1.0.0-SNAPSHOT.jar" ]
