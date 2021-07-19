# Architecture

The system uses the
standard [Spring Framework](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#spring-webflux)
architecture. Requests enter through the controller, which handles the HTTP level logic, and delegates more specific
functionality to either services or repositories, depending on the situation. This system is also built on reactive
technologies. This uses Kotlin Coroutines to integrate with Spring WebFlux.

# API

API information is served by the application itself. OpenAPI specs can be accessed through `/v3/api-docs` (JSON)
or `/v3/api-docs.yaml` (YAML). This does not supply a Swagger UI, but an external UI can be used
(e.g. https://editor.swagger.io/).

# Development Setup

All initial operations begin relative to root project directory.

## Docker

Docker components are defined under `dev/`. If running the app separately, simply comment out the `services.users` block
in the `docker-compose.yml` file.

```BASH
$ cd dev/
$ docker-compose up 
```

## Manual

Gradle is used to build the project. Java 11 or later is required to build and run the project.

### Testing

```BASH
$ ./gradlew test
```

### Building

```BASH
$ ./gradlew clean bootJar
```

### Running

The [Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/appendix-application-properties.html#common-application-properties)
and [SpringDoc OpenAPI](https://springdoc.org/#properties) properties can be used to configure the application. By
default the application will listen on port 8080 and connects to a MongoDB instance on port 27017.

```BASH
$ java -jar build/libs/users-1.0.0-SNAPSHOT.jar
```