# webclient-showcase
Demo project showing various applications of Spring WebFlux &amp; WebClient in a Microservices setup

## Introduction/scope

The servlet specification was built with the blocking semantics or one-request-per-thread model. 
Usually, in a cloud environment, the machines are smaller than traditional data-centers. 
Instead of a big machine, it is popular to use many small machines and try to scale applications horizontally. 
In this scenario, the servlet spec can be switched to an architecture created upon Reactive Streams. 
This kind of architecture fits better than servlet for the cloud environments.
Spring Framework has been creating the Spring WebFlux to helps developers to create Reactive Web Applications [[1]](https://www.packtpub.com/application-development/developing-java-applications-spring-and-spring-boot).

In this demo we will primarily focus on the reactive WebClient component making calls to remote services which is actually a good starting point
and a pretty common case. As stated in [[2]](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice) 
the greater the latency per call or the interdependency among calls, the more dramatic the performance benefits are. 
An extra motivation for this approach is the fact that since Spring version 5.0, 
the `org.springframework.web.client.RestTemplate` class is in maintenance mode, with only minor requests for changes and bugs to be accepted 
going forward. Therefore, it is advised to start using the `org.springframework.web.reactive.client.WebClient` which has a more modern API.
Moreover it supports sync, async, and streaming scenarios.

We built a sample application based on a minimal Microservices architecture and demonstrate a number of capabilities driven by the requirements
of each use-case. With the reactive WebClient we can return reactive types (e.g. Flux or Mono) directly from Spring MVC controller methods.
Spring MVC controllers can call other reactive components too. A mix is also possible in case we have some endpoints and services which cannot
become reactive for a number of reasons such as: blocking dependencies with no reactive alternatives or we may have an existing legacy app 
which we want to migrate gradually etc.

## Project structure

In order to demonstrate the usage of WebClient for service-to-service communication we will use Spring Cloud with [HashiCorp Consul](https://www.consul.io) for service discovery.
Apart from this each microservice will be based on Spring Boot and we will also bring Spring Data R2DBC into play in order to integrate with
a PostgreSQL database using a reactive driver. A diagram of our components is shown below:

[diagram]

We have Integration tests covering each microservice endpoint and we use [WireMock](http://wiremock.org) and [Testcontainers](https://www.testcontainers.org) for this purpose. 

### WebClient simple usage

### Parallel calls to the same endpoint

### Parallel calls to different endpoints

### .zip VS .zipWhen VS .zipDelayError VS .zipOnNext

### .doOnNext VS .doOnSuccess VS .doOnError

### Chaining of calls (Sequential execution)

### Service-to-service communication

We can leverage the WebClient for service-to-service communication as well instead of [Spring Cloud OpenFeign](https://spring.io/projects/spring-cloud-openfeign) or other methods. 
We usually want to take advantage of client-side load balancing too and this is possible by applying the `@LoadBalanced` annotation.

However a couple of practical issues that you may face with real-world applications are:
* the need for [Multiple WebClient Objects](https://cloud.spring.io/spring-cloud-commons/2.1.x/multi/multi__spring_cloud_commons_common_abstractions.html#_multiple_webclient_objects)
* propagate a JWT token in case we have our various endpoints protected

### Database interaction (r2dbc/postgresql)

### Other topics

#### BlockHound

#### Logging

#### Async SOAP
Based on [6] but with ApacheCXF instead

### How to run

#### Integration tests
mvn clean verify

#### Live tests
docker-compose up

## Conclusion

Performance has many characteristics and meanings. Reactive and non-blocking generally do not make applications run faster. 
They can, in some cases, for example when using the WebClient to run remote calls in parallel while at the same time avoiding
getting involved with Task Executors and use a more elegant and fluent API instead. It comes of course with a learning curve.

The key expected benefit of reactive and non-blocking is the ability to scale with a small, fixed number of threads and less memory. 
That makes applications more resilient under load, because they scale in a more predictable way. 
In order to observe those benefits, however, you need to have some latency (including a mix of slow and unpredictable network I/O). 
That is where the reactive stack begins to show its strengths, and the differences can be dramatic [3].

Some interesting load testing and comparison results are presented at [Spring Boot performance battle: blocking vs non-blocking vs reactive](https://medium.com/@filia.aleks/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0). 
The conclusion is that Spring Webflux with WebClient and Apache clients wins in all cases. 
The most significant difference (4 times faster than blocking Servlet) when underlying service is slow (500ms). 
It's 15–20% faster then Non-blocking Servlet with `CompetableFuture`. Also, it does not create a lot of threads comparing with Servlet (20 vs 220).

## References

[1] https://www.packtpub.com/application-development/developing-java-applications-spring-and-spring-boot

[2] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice

[3] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-performance

[4] https://medium.com/@filia.aleks/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0

[5]* https://allegro.tech/2019/07/migrating-microservice-to-spring-webflux.html

[6] https://godatadriven.com/blog/reactive-web-service-client-with-jax-ws/

[7]* https://www.youtube.com/watch?v=IZ2SoXUiS7M&t=11s (Guide to "Reactive" for Spring MVC Developers, by Rossen Stoyanchev)