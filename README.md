![Java CI with Maven](https://github.com/kmandalas/webclient-showcase/workflows/Java%20CI%20with%20Maven/badge.svg)

# webclient-showcase
This article aims to be an introduction to developing Reactive Microservices based on the Spring framework. We combine material
from various official and 3rd party sources along with hints and lessons learned from personal experience putting together a real-world application. 
The goal is to present the benefits and the capabilities but also the limitations and challenges from adopting such technology. 

We will present all these while building a demo project showing various applications of Spring WebFlux &amp; WebClient in a Microservices setup.
We will follow a business problem-solution approach to make things more realistic. This is not intended to cover the majority of the reactive APIs
but should be enough to give you a good idea what lies ahead if you enter this domain and the learning curve required. 

Apart from Java understanding, a familiarity with Spring Cloud Netflix stack is required, basic knowledge of [Project Reactor](https://projectreactor.io) 
(what is a Mono, what is a Flux) and the very basics of Docker.

## Introduction/scope

The servlet specification was built with the blocking semantics or one-request-per-thread model. 
Usually, in a cloud environment, the machines are smaller than traditional data-centers. 
Instead of a big machine, it is popular to use many small machines and try to scale applications horizontally. 
In this scenario, the servlet spec can be switched to an architecture created upon Reactive Streams. 
This kind of architecture fits better than servlet for the cloud environments.
Spring Framework has been creating the Spring WebFlux to helps developers to create Reactive Web Applications [[1]](https://www.packtpub.com/application-development/developing-java-applications-spring-and-spring-boot).

Spring WebFlux, which is based on Project Reactor, allows us to:
* move from blocking to non-blocking code and do more work with fewer resources
* increase potential to handle massive numbers of concurrent connections
* satisfy more concurrent users with fewer microservice instances
* apply back-pressure and ensure better resilience between decoupled components

In our demo project we will center around the reactive WebClient component making calls to remote services. This is actually a good starting point
and a pretty common case. As stated in [[2]](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice) 
the greater the latency per call or the interdependency among calls, the more dramatic the performance benefits are. 
An extra motivation for this approach is the fact that since Spring version 5.0, 
the `org.springframework.web.client.RestTemplate` class is in maintenance mode, with only minor requests for changes and bugs to be accepted 
going forward. Therefore, it is advised to start using the `org.springframework.web.reactive.client.WebClient` which has a more modern API.
Moreover it supports sync, async, and streaming scenarios.

We will build a sample application based on a minimal Microservices architecture and demonstrate a number of capabilities driven by the requirements
of each use-case. With the reactive WebClient we can return reactive types (e.g. Flux or Mono) directly from Spring MVC controller methods.
Spring MVC controllers can call other reactive components too. A mix is also possible in case we have some endpoints and services which cannot
become reactive for a number of reasons such as: blocking dependencies with no reactive alternatives or we may have an existing legacy app 
which we want to migrate gradually etc. In our case we will follow the [Annotated Controllers programming model](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-programming-models)

## Scenario / Project structure

We are going to implement a simplified One Time Password (OTP) service, offering the following capabilities:

* Generate OTP
* Validate (use) OTP
* Resend OTP
* Get OTP status
* Get all OTPs

Our application will consist of the following microservices:

* **otp-service:** which will provide the functionality above by orchestrating calls to local and remote services 
* **customer-service:** will keep a catalogue of registered users to our service with information like: account id, MSISDN, e-mail etc.

A number of remote (external) services will be invoked. We assume that our application is authorized to use them will access them via their REST API.
Of course these will be mocked for simplicity. These "3rd-party" services are:

* **number-information:** takes a phone number as input and verifies that it belongs to a Telecoms operator and is currently active
* **notification-service:** delivers the generated OTPs to the designated number or channel (phone, e-mail, messenger etc.)

In order to simulate a microservices setup, we will use Spring Cloud with [HashiCorp Consul](https://www.consul.io) for service discovery and
Spring Cloud Gateway. There is no particular reason for not going with Eureka, just have in mind that Consul can play the role of a 
centralized configuration server as well while with Eureka only we need to have a separate [Spring Cloud Config](https://cloud.spring.io/spring-cloud-config/reference/html/) 
server.

We choose Spring Cloud Gateway instead of *Zuul* for the following reasons:
* Spring Cloud Gateway is reactive by nature and runs on Netty
* Spring Team has moved most of Spring Cloud Netflix components (Ribbon, Hystrix, Zuul) into maintenance mode
* Spring Team does not intend to port-in *Zuul 2* which is also reactive in contrast to *Zuul 1*

We will go with Spring Cloud Loadbalancer (instead of *Ribbon*) for client-side load balancing and with 
**@LoadBalanced WebClient** (instead of *Feign*) for service-to-service communication. Apart from this, each microservice will be based on 
Spring Boot. We will also bring Spring Data R2DBC into play in order to integrate with a PostgreSQL database using a reactive driver. 
A diagram of our components is shown below:

![Image of Microservices](/diagrams/WebClientShowcase.png)

#### generate OTP

Business requirement

> Given the number of a user in [E.164](https://en.wikipedia.org/wiki/E.164) format: 
> 1. fetch customer data from **customer-service** and in parallel validate the number status using the **number-information** service
> 2. produce an OTP pin and save it in the DB
> 3. invoke the **notification-service** to deliver it
> 4. return response 

Solution
 
First of all, we see that we need to communicate with 1 internal microservice (service-to-service communication) and with 2 external (remote) services.

As already mentioned we choose to go with `@LoadBalanced WebClient`, therefore we you need to have a *loadbalancer* implementation in the classpath. 
In our case we have added the `org.springframework.cloud:spring-cloud-loadbalancer` dependency to the project. This way, a `ReactiveLoadBalancer` 
will be used under the hood. Alternatively, this functionality could also work with `spring-cloud-starter-netflix-ribbon`, 
but the request would then be handled by a non-reactive `LoadBalancerClient` plus, like we said, *Ribbon* is already in maintenance mode, 
so it is not recommended for new projects [[10]](https://cloud.spring.io/spring-cloud-static/spring-cloud-commons/2.1.6.RELEASE/multi/multi__spring_cloud_commons_common_abstractions.html#_spring_webclient_as_a_load_balancer_client)

One more thing we need, is to disable *Ribbon* in the application properties of our services:
```
spring:
    loadbalancer:
      ribbon:
        enabled: false
```

Finally a note about *Feign* which was a quite popular choice till now along with *Ribbon*: the [OpenFeign](https://github.com/OpenFeign/feign) 
project does not currently support reactive clients, neither does Spring Cloud OpenFeign. Therefore we will not use it. For more details
check [here](https://cloud.spring.io/spring-cloud-openfeign/reference/html/#reactive-support)

Now, here are a couple of practical issues that one may face with real-world applications:
1. the need for [Multiple WebClient Objects](https://cloud.spring.io/spring-cloud-commons/2.1.x/multi/multi__spring_cloud_commons_common_abstractions.html#_multiple_webclient_objects)
2. to propagate a JWT token in case we have our various endpoints protected

In order to deal with the 1st issue, we will declare 2 different WebClient Beans inside our `WebClientConfig` class.
This is necessary since service-discovery and load-balancing is only applicable to our own domain and services. 
Therefore we need to use different instances of WebClient Beans which of course may have additional differences in configuration (e.g. timeouts)
than the `@LoadBalanced` annotation.

For the 2nd issue we need to propagate the access token within the `header` attribute of the WebClient:
```
.header("Authorization", String.format("%s %s", "Bearer", tokenUtils.getAccessToken()))
```

In the snippet above, we assume we have a utility method that gets a JWT token from the incoming request forwarded via Spring Cloud Gateway 
to the **otp-service**. We use this in order to set the "Authorization" header attribute with the value of the Bearer token effectively passing it 
on to **the customer-service**. Keep in mind that the following settings are also needed in the [application.yml](https://github.com/kmandalas/webclient-showcase/blob/master/gateway-service/src/main/resources/application.yml) 
of the **gateway-service** in order to allow this relay:
```
globalcors:
        cors-configurations:
          '[/**]':
            allowedOrigins: ["*"]
            allowedMethods: ["POST","GET","DELETE","PUT"]
            allowedHeaders: "*"
            allowCredentials: true
```

Now that we have these sorted out, let's see which Reactor Publisher functions we can use to get the result:

* In order to make parallel calls to different endpoints we will use Mono's ***zip*** method. If an error occurs in one of the Monos, 
the execution stops immediately. If we want to delay errors and execute all Monos, then we can use ***zipDelayError*** instead
* When these parallel calls complete in order to process the results, chain subsequent actions and return a response, we will use the ***flatMap*** method
* Inside the transformer Function of the ***flatMap***, we generate a random PIN and we persist in in the DB using a `ReactiveCrudRepository`
* We use the ***zipWhen*** method to trigger the notification-service only after the DB interaction has finished
* Finally, we use ***map*** method in order to select our return value which in our case is the data object that was previously saved in the DB

For a full list of options you may check the [Mono API](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html).

### Other topics

#### Logging

Logging is an important aspect for every kind of software. Solutions based on Microservices architectures have additional demands for
centralized logging. However when we are using File Appenders for logging then we have an issue since this I/O operation is blocking.
See the following issue for example:
* https://github.com/spring-projects/spring-framework/issues/25547

A solution is to select and configure Async Appenders which seem to be supported by major SLF4J implementations like Log4j and Logback.
In our example we go with the [Logback AsyncAppender](http://logback.qos.ch/manual/appenders.html#AsyncAppender). An example configuration
can be seen [here](https://github.com/kmandalas/webclient-showcase/blob/master/otp-service/src/main/resources/logback-spring.xml).

The AsyncAppender has five (5) configuration options: 
   
* **_queueSize_** – The maximum capacity of the buffer size. Default value is 256.
* **_discardingThreshold_** – Instruct to drop events once the buffer reaches the max capacity. Default value is 20%.
* **_neverBlock_** – Setting it to true will prevent any blocking on the application threads but it comes at the cost of lost log events if 
the AsyncAppender’s internal buffer fills up. Default value is false.
* **_includeCallerData_** – Extracting caller data. Default value is false.
* **_maxFlushTime_** – Specify a maximum queue flush timeout in milliseconds

#### Distributed Tracing

Tracing is another vital aspect of Microservices monitoring. We can trace all calls that are made from/to the microservices, 
using [Spring Cloud Sleuth](https://docs.spring.io/spring-cloud-sleuth/docs/current-SNAPSHOT/reference/html/) and [Jaeger](https://www.jaegertracing.io/).

Sleuth offers a convenient auto-configuration that works out-of-the-box with popular frameworks like Spring MVC and Webflux.
It allows injecting trace and span IDs automatically and displaying this information in the logs, as well as annotation-based span control.
In order to make it work with Jaeger, we need to enable the *Zipkin* collector port in Jaeger's configuration.

One thing to have in mind is that limitations do exist here as well. For example tracing database calls with R2DBC is not yet supported. 
You may find the related issue here:
* https://github.com/spring-cloud/spring-cloud-sleuth/issues/1524

Following is screenshot of Jarger UI homepage:

![Jaeger Home](/diagrams/jaeger-home.png)

And this is an example of tracing the call which generates OTPs:
![Jaeger Trace Details](/diagrams/jaeger-trace.png)


#### Reactive types support for @Cacheable methods

Spring's `@Cacheable` annotation is a convenient approach to handle caching usually at the services level. This cache abstractions works
seamlessly with various caching implementations including JSR-107 compliant caches, Redis etc. However, at the moment of writing there is
still no Reactive types support for `@Cacheable` methods. The related is issue is:

* https://github.com/spring-projects/spring-framework/issues/17920

And although Redis is a pretty common centralized cache solution, and a Reactive driver for Redis exists is Spring Data project, 
there is no plan at the moment to add a reactive cache implementation: 

* https://jira.spring.io/browse/DATAREDIS-967

#### Handling special cases
On a typical Spring WebFlux server (Netty), you can expect one thread for the server and several others for request processing 
which are typically as many as the number of CPU cores. Although WebClient does not block the thread, sometimes it is desired to use 
another thread pool than the main worker thread pool shared with the server. Such cases may be calls to remote endpoints with very long 
response times or increased level of concurrency e.g. we want to submit 10 calls in parallel cause we know we can afford it and it suits our scenario :)

For this purpose, Spring WebFlux provides thread pool abstractions called **Schedulers**. You may use it to create different concurrency strategies. 
If you prefer to have a full control of the minimum and maximum number of threads in the pool you should define your own task executor as 
shown below:
```
Bean
public ThreadPoolTaskExecutor taskExecutor() {
   ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
   executor.setCorePoolSize(5);
   executor.setMaxPoolSize(10);
   executor.setQueueCapacity(100);
   executor.setThreadNamePrefix("otp-");
   executor.initialize();
   return executor;
}
```

Let's now imagine that we want to check the status of five MSISDNs simultaneously and return the result as a list of NotificationResultDTOs:
```
public Flux<NotificationResultDTO> checkMsisdns(List<String> msisdns) {
    return Flux.fromIterable(msisdns)
        .parallel()
        .runOn(Schedulers.fromExecutor(taskExecutor))
        .flatMap(this::checkMsisdn)
        .sequential() 
}
```

At the point where we call the ***parallel*** method, it creates a _ParallelFlux_. This indicates the simultaneous character of the execution.
At the ***runOn*** method we plugin our task executor and finally we need to specify how to convert _ParallelFlux_ to simple _Flux_. We select
to do this in a sequential manner while other options exist for the results to follow specific order. For a full list of options you may
check the [ParallelFlux API](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/ParallelFlux.html).

#### BlockHound

The change from blocking to non-blocking code or from imperative to reactive programming is tricky and demands to build up a level of
experience in order to make your self comfortable. Sometimes it may be hard to detect blocking code in Reactor thread. 
And this is because we don't need to use ***.block()*** to make things blocking but we can unconsciously introduce blocking by using a 
library which can block the current thread.

A useful utility to helps us detect some cases is [BlockHound](https://github.com/reactor/BlockHound). It is sponsored by Pivotal and it can 
be used in various ways but we recommend to used during the test phase. The only thing you need for this is to include the following dependency:

```
<!-- https://mvnrepository.com/artifact/io.projectreactor.tools/blockhound-junit-platform -->
<dependency>
    <groupId>io.projectreactor.tools</groupId>
    <artifactId>blockhound-junit-platform</artifactId>
    <version>1.0.4.RELEASE</version>
</dependency>
```

Keep in mind that if go with Java 11 and above, the following JVM argument is needed for the tool to work properly: 
```
-XX:+AllowRedefinitionToAddDeleteMethods
```

#### Testing & debugging

In our sample project we show an example Integration Test covering our most "complicated" endpoint which is the one that generates an OTP. 
We use [HoverFly](http://hoverfly.io) for mocking responses of the two "external" services (i.e. number-information and notification-service) 
and the call to our "internal" service (i.e. customer-service). We also use [Testcontainers](https://www.testcontainers.org) for spinning-up 
a dockerized PostgresDB during the test's execution. 

The full code can be seen in [OTPControllerIntegrationTests](https://github.com/kmandalas/webclient-showcase/blob/master/otp-service/src/test/java/gr/kmandalas/service/otp/OTPControllerIntegrationTests.java) class.

We also use `WebTestClient` which is a Client for testing web servers that uses WebClient internally to
perform requests while also providing a fluent API to verify responses. This client can connect to any server over HTTP, or to a WebFlux 
application via mock request and response objects.

One thing worthwhile to mention is the "trick" we perform in order to simulate the existence of **customer-service** `ServiceInstance`.
A `ServiceInstance` represents an instance of a service in a discovery system. When running integration tests, we usually have part of cloud 
features disabled and service discovery is one of them. However, since we use `@LoadBalanced` WebClient when we invoke the **customer-service**
during the integration flow we test, we need a way to simulate a "static" instance of this service. Moreover we need to "bind" it with
HoverFly so when it's actually invoked to return the mocked response we want. This is achieved with the 
`gr.kmandalas.service.otp.OTPControllerIntegrationTests.TestConfig` static class.

#### Async SOAP

Nowadays the majority of the systems we integrate with expose REST endpoints. Its not uncommon however to still have to integrate with
SOAP-based web services. Both JAX-WS and ApacheCXF allow the generation of non-blocking clients. You may find an example of how to deal
with such case at [Reactive Web Service Client with JAX-WS](https://godatadriven.com/blog/reactive-web-service-client-with-jax-ws/)

### How to run

The easiest way is to run the microservices using Docker Compose

> docker-compose up --build

When the containers are up and running, you can visit consul's UI to see the active services

> http://localhost:8500/ui/dc1/services

![Image of Consul console](/diagrams/consul.png)

Below you may find `curl` commands for invoking the various endpoints via our API Gateway:

**Generate OTP** 
```
curl --location --request POST 'localhost:8000/otp-service/v1/otp' \
--header 'Content-Type: application/json' \
--data-raw '{
    "msisdn": "00306933177321"
}'
```

**Get All OTP** 
```
curl --location --request GET 'localhost:8000/otp-service/v1/otp'
```

**Resend OTP** 
```
curl --location --request POST 'localhost:8000/otp-service/v1/otp/2?via=AUTO,EMAIL,VOICE&mail=john.doe@gmail.com' \
--header 'Content-Type: application/json' \
```

**Validate OTP** 
```
curl --location --request POST 'http://localhost:8000/otp-service/v1/otp/36/validate?pin=356775' \
```

## Conclusion

Performance has many characteristics and meanings. Reactive and non-blocking generally do not make applications run faster. 
They can, in some cases, for example when using the WebClient to run remote calls in parallel while at the same time avoiding
getting involved with Task Executors and use a more elegant and fluent API instead. It comes of course with a learning curve.

The key expected benefit of reactive and non-blocking is the ability to scale with a small, fixed number of threads and less memory. 
That makes applications more resilient under load, because they scale in a more predictable way. 
In order to observe those benefits, however, you need to have some latency (including a mix of slow and unpredictable network I/O). 
That is where the reactive stack begins to show its strengths, and the differences can be dramatic [[3]](https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-performance).

Some interesting load testing and comparison results are presented at [[4]](https://medium.com/@filia.aleks/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0). 
The conclusion is that Spring Webflux with WebClient and Apache clients "win" in all cases. 
The most significant difference (4 times faster than blocking Servlet) comes when underlying service is slow (500ms). 
It's 15–20% faster then non-blocking Servlet with `CompetableFuture`. Also, it does not create a lot of threads comparing with Servlet (20 vs 220).

## References

[1] https://www.packtpub.com/application-development/developing-java-applications-spring-and-spring-boot

[2] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-framework-choice

[3] https://docs.spring.io/spring/docs/current/spring-framework-reference/web-reactive.html#webflux-performance

[4] https://medium.com/@filia.aleks/microservice-performance-battle-spring-mvc-vs-webflux-80d39fd81bf0

[5]* https://allegro.tech/2019/07/migrating-microservice-to-spring-webflux.html

[6] https://godatadriven.com/blog/reactive-web-service-client-with-jax-ws/

[7]* https://www.youtube.com/watch?v=IZ2SoXUiS7M (Guide to "Reactive" for Spring MVC Developers, by Rossen Stoyanchev)

[8] https://www.baeldung.com/spring-webflux-concurrency

[9] https://piotrminkowski.com/2020/03/30/a-deep-dive-into-spring-webflux-threading-model/

[10] https://cloud.spring.io/spring-cloud-static/spring-cloud-commons/2.1.6.RELEASE/multi/multi__spring_cloud_commons_common_abstractions.html#_spring_webclient_as_a_load_balancer_client

### TODO

- [x] WebClient simple usage
- [x] Parallel calls to the same endpoint
- [x] Parallel calls to the different endpoint
- [x] .zip
- [x] .zipWhen
- [x] .zipDelayError
- [ ] .doOnNext
- [x] .doOnSuccess VS .doOnError
- [x] Chaining of calls (Sequential execution)
- [x] Service-to-service communication
- [x] Database interaction (r2dbc/postgresql)
