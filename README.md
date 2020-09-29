![Java CI with Maven](https://github.com/kmandalas/webclient-showcase/workflows/Java%20CI%20with%20Maven/badge.svg)

# webclient-showcase
This projects article aims to be an introduction to developing Reactive Microservices based on the Spring framework. 

![Image of Microservices](/diagrams/WebClientShowcase.png)


### How to run

In order to build and test the application, the prerequisites are:
* Java 11 and above
* Maven
* Docker (because we use TestContainers during our Integration tests)

Then simply execute a `mvn clean verify`

The easiest way is to run the microservices using Docker and Docker Compose:

> docker-compose up --build

When the containers are up and running, you can visit consul's UI to see the active services:

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

**Validate OTP** 
```
curl --location --request POST 'http://localhost:8000/otp-service/v1/otp/36/validate?pin=356775' \
```

**Resend OTP** 
```
curl --location --request POST 'localhost:8000/otp-service/v1/otp/2?via=AUTO,EMAIL,VOICE&mail=john.doe@gmail.com' \
--header 'Content-Type: application/json' \
```

**Get All OTPs** 
```
curl --location --request GET 'localhost:8000/otp-service/v1/otp?number=00306933177321'
```

**OTP Status** 
```
curl --location --request GET 'localhost:8000/otp-service/v1/otp/1'
```

### Things covered

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
