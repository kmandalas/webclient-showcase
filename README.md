![Java CI with Maven](https://github.com/kmandalas/webclient-showcase/workflows/Java%20CI%20with%20Maven/badge.svg)

# webclient-showcase
This projects article aims to be an introduction to developing Reactive Microservices based on the Spring framework.

We are going to implement a simplified One Time Password (OTP) service, offering the following capabilities:

* Generate OTP
* Validate (use) OTP
* Resend OTP
* Get OTP status
* Get all OTPs of a given number

Our application will consist of the following microservices:

* **otp-service:** which will provide the functionality above by orchestrating calls to local and remote services 
* **customer-service:** will keep a catalogue of registered users to our service with information like: account id, MSISDN, e-mail etc.

A number of remote (external) services will be invoked. We assume that our application is authorized to use them will access them via their REST API.
Of course these will be mocked for simplicity. These "3rd-party" services are:

* **number-information:** takes a phone number as input and verifies that it belongs to a Telecoms operator and is currently active
* **notification-service:** delivers the generated OTPs to the designated number or channel (phone, e-mail, messenger etc.)


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
