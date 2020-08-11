# webclient-showcase
Demo project showing various applications of Spring WebFlux &amp; WebClient in a Microservices setup

## Introduction/scope

The servlet specification was built with the blocking semantics or one-request-per-thread model. 
Usually, in a cloud environment, the machines are smaller than traditional data-centers. 
Instead of a big machine, it is popular to use many small machines and try to scale applications horizontally. 
In this scenario, the servlet spec can be switched to an architecture created upon Reactive Streams. 
This kind of architecture fits better than servlet for the cloud environments.
Spring Framework has been creating the Spring WebFlux to helps developers to create Reactive Web Applications [1].

In this demo we will focus on the reactive WebClient component making calls to remote services. 
We built various scenarios based on a minimal Microservices architecture and demonstrate a number of capabilities by example.
With the reactive WebClient we can return reactive types (Reactor, RxJava, or other) directly from Spring MVC controller methods. 
The greater the latency per call or the interdependency among calls, the more dramatic the benefits. 
Spring MVC controllers can call other reactive components too [2].


### WebClient simple usage

### Parallel calls to the same endpoint

### Parallel calls to different endpoints

### .zip VS .zipWhen VS .zipDelayError VS .zipOnNext VS 

### .doOnNext VS .doOnSuccess VS .doOnError

### Chaining of calls (Sequential execution)

### Service-to-service communication

### Database interaction (r2dbc/postgresql)

### Other topics

#### Logging

#### Async SOAP

## Conclusion

Performance has many characteristics and meanings. Reactive and non-blocking generally do not make applications run faster. 
They can, in some cases, (for example, if using the WebClient to run remote calls in parallel). 
On the whole, it requires more work to do things the non-blocking way and that can slightly increase the required processing time.

The key expected benefit of reactive and non-blocking is the ability to scale with a small, fixed number of threads and less memory. 
That makes applications more resilient under load, because they scale in a more predictable way. 
In order to observe those benefits, however, you need to have some latency (including a mix of slow and unpredictable network I/O). 
That is where the reactive stack begins to show its strengths, and the differences can be dramatic [3].

## References

[1] https://www.packtpub.com/application-development/developing-java-applications-spring-and-spring-boot

[2] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice

[3] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-performance

