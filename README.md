# Java HttpClient for Spring Web Client Boot Starter
[![javadoc](https://javadoc.io/badge2/com.integralblue/java-httpclient-webclient-spring-boot-starter/javadoc.svg)](https://javadoc.io/doc/com.integralblue/java-httpclient-webclient-spring-boot-starter)
[![Maven Central](https://img.shields.io/maven-central/v/com.integralblue/java-httpclient-webclient-spring-boot-starter)](https://search.maven.org/artifact/com.integralblue/java-httpclient-webclient-spring-boot-starter)
![GitHub](https://img.shields.io/github/license/candrews/java-httpclient-webclient-spring-boot-starter)

The Java HttpClient for Spring Web Client Boot Starter provides a quick and easy way to use [Java 11's HttpClient](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/HttpClient.html) as [Spring WebClient's](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-webclient) client HTTP connector.

By default, [Spring WebClient will try to use Reactor Netty then Jetty Client as it's client HTTP connector](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-webclient-runtime). This starter will instead use Java 11's HTTP client.

Benefits of using this starter include:
* not needing additional dependencies such as on Netty or Eclipse
* getting the benefits and features of Java's built in HttpClient
* Being able to use tooling for Java's HttpClient, such as [HttpClientMock](https://github.com/PGSSoft/HttpClientMock)

## Using This Starter
To use this starter, you need:
* Java 11 (or later)
* Spring Boot 2.3.0 (or later)
* Spring WebClient, which is included in Spring WebFlux

### Add the Dependency

Add a dependency on this starter your dependency manager of choice, making sure to use the latest version: ![Maven Central](https://img.shields.io/maven-central/v/com.integralblue/java-httpclient-webclient-spring-boot-starter)

For Gradle:
```groovy
dependencies {
	runtimeOnly 'com.integralblue:java-httpclient-webclient-spring-boot-starter:VERSION'
}
```

For Maven:
```xml
<dependency>
	<groupId>com.integralblue</groupId>
	<artifactId>java-httpclient-webclient-spring-boot-starter</artifactId>
	<version>VERSION</version>
    <scope>runtime</scope>
</dependency>
```

### Use the Autowired WebClient.Builder

Follow the advice given in the Spring Boot documentation under [Calling REST Services with WebClient](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-webclient) when using `WebClient` by autowiring an instance of `WebClient.Builder` and using that to create the `WebClient` instance.

For example:
```java
@Service
public class MyService {

    private final WebClient webClient;

    public MyService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://example.org").build();
    }

    public Mono<Details> someRestCall(String name) {
        return this.webClient.get().uri("/{name}/details", name)
                        .retrieve().bodyToMono(Details.class);
    }
```

`WebClient.create()` and `WebClient.builder().build()` will not apply the customizations from this starter meaning `WebClient`s created using those approaches will not use Java 11's HttpClient.

## Future of This Starter
Eventually, a future version of Spring will likely include this functionality. Follow [the Pull Request for JDK 11 HttpClient integration with WebClient](https://github.com/spring-projects/spring-framework/pull/23432/) for more information.

## Developing the Starter
This project requires Java 11 (or later).
Import this Gradle project using your IDE of choice.
Or, if you don't want to use an IDE, you can run the project from the command line: `./gradlew build` The test suite will run and a jar will be output at [build/libs/](build/libs/).

### Eclipse
The Project Lombok Eclipse integration must be setup. See the Eclipse instructions on [Project Lombok's site](https://projectlombok.org/features/index.html).