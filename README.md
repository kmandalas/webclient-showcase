# webclient-showcase
Demo project showing various applications of Spring WebFlux &amp; WebClient in a Microservices setup

## Introduction/scope

The servlet specification was built with the blocking semantics or one-request-per-thread model. 
Usually, in a cloud environment, the machines are smaller than traditional data-centers. 
Instead of a big machine, it is popular to use many small machines and try to scale applications horizontally. 
In this scenario, the servlet spec can be switched to an architecture created upon Reactive Streams. 
This kind of architecture fits better than servlet for the cloud environments.
Spring Framework has been creating the Spring WebFlux to helps developers to create Reactive Web Applications [1].

In this demo we will primarily focus on the reactive WebClient component making calls to remote services. 
We built various scenarios based on a minimal Microservices architecture and demonstrate a number of capabilities by example.
With the reactive WebClient we can return reactive types (Reactor, RxJava, or other) directly from Spring MVC controller methods.
Spring MVC controllers can call other reactive components too. 

The greater the latency per call or the interdependency among calls, the more dramatic the benefits [2].

**NOTE:** As of Spring version 5.0 the `org.springframework.web.client.RestTemplate` class is in maintenance mode, 
with only minor requests for changes and bugs to be accepted going forward. 
Therefore, it is advised to start using the `org.springframework.web.reactive.client.WebClient` which has a more modern API.
Moreover it supports sync, async, and streaming scenarios.

### WebClient simple usage

### Parallel calls to the same endpoint

### Parallel calls to different endpoints

### .zip VS .zipWhen VS .zipDelayError VS .zipOnNext

### .doOnNext VS .doOnSuccess VS .doOnError

### Chaining of calls (Sequential execution)

### Service-to-service communication

### Database interaction (r2dbc/postgresql)

### Other topics

#### BlockHound

#### Logging

#### Async SOAP
Based on [6] but with ApacheCXF instead

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