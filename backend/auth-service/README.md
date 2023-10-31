# Authentication Service

A Java spring-boot authentication microservice. Uses Spring Security and Spring Boot for creating a JWT based Authentication Service.

## Features

- API Endpoints to register and authenticate providing you with JWT and Refersh Token.
- Refresh Tokens are maintained in DB for convenience. 
- Using the /refershToken endpoint, providing the refreshToken returns a valid JWT without authentication.

## Pre-requisites

Following are the requirements for the authentication service:
- JDK 17
- Apache Maven

## Development

Simply open the auth-service folder in IntelliJ IDEA and load the Maven changes with the above requirements satisfied.

## Build

Maven is used as the dependency Management and build tool for this service. Use the following command to build the JAR:

```mvn clean package```

## Testing

Testing for the service is divided into Unit Tests and Integration Tests. A seperate maven profile is used for Unit tests and Integration tests so that they can be executed independently of each other as Integration Tests load the complete Spring Context which can be Time-consuming.

### Unit Tests
To execute Unit Tests using Maven. Use the following command:

```mvn test -Punit-test```

### Integration Tests
Integration Tests are executed using TestContainers which provides a Containerized Database for Testing purposes. Using Spring MockMVC in conjunction with TestContainers, we can write comprehensive Integration Tests.

To execute the Integration Tests using Maven. Use the following command:

```mvn test -Pintegration-test```
