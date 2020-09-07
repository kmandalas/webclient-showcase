package gr.kmandalas.service.otp.service;

import gr.kmandalas.service.otp.dto.CustomerDTO;
import gr.kmandalas.service.otp.dto.NotificationRequestForm;
import gr.kmandalas.service.otp.dto.NotificationResultDTO;
import gr.kmandalas.service.otp.dto.SendForm;
import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.enumeration.OTPStatus;
import gr.kmandalas.service.otp.exception.OTPException;
import gr.kmandalas.service.otp.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;

import static org.springframework.data.mapping.Alias.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {

  private final OTPRepository otpRepository;

  @Autowired
  @LoadBalanced
  private WebClient.Builder loadbalanced;

  @Autowired
  private WebClient.Builder webclient;

  @Value("${external.services.number-information}")
  private String numberInformationServiceUrl;

  @Value("${external.services.notifications}")
  private String notificationServiceUrl;

  /**
   * Read all OTP's
   * @param customerId optional filter of customerId
   */
  public Flux<OTP> getAll(Long customerId) {
    return otpRepository.findAll()
        .filter(otp -> !ofNullable(customerId).isPresent() || otp.getCustomerId().equals(customerId));
  }

  /**
   * Read an already generated OTP
   * @param otpId the OTP id
   */
  public Mono<OTP> get(Long otpId) {
    return otpRepository.findById(otpId)
        .switchIfEmpty(Mono.error(new OTPException(HttpStatus.NOT_FOUND, "OTP not found")));
  }

  /**
   * Generate and send an OTP
   * @param form the form
   */
  public Mono<OTP> send(SendForm form) {
    String customerURI = UriComponentsBuilder
            .fromHttpUrl("http://customer-service/customers")
            .queryParam("number", form.getMsisdn())
            .toUriString();

    String numberInfoURI = UriComponentsBuilder
            .fromHttpUrl(numberInformationServiceUrl)
            .queryParam("msisdn", form.getMsisdn())
            .toUriString();

    // 1st call to customer-service using service discovery, to retrieve customer related info
    Mono<CustomerDTO> customerInfo = loadbalanced.build()
            .get()
            .uri(customerURI)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError,
                    clientResponse -> Mono.error(new OTPException(HttpStatus.BAD_REQUEST, "Error retrieving Customer")))
            .bodyToMono(CustomerDTO.class);

    // 2nd call to external service, to check that the MSISDN is valid
    Mono<String> msisdnStatus = webclient.build()
            .get()
            .uri(numberInfoURI)
            .exchange()
            .flatMap(clientResponse -> {
                if (!clientResponse.statusCode().is2xxSuccessful())
                    return Mono.error(new OTPException(HttpStatus.BAD_REQUEST, "Error retrieving msisdn info"));
                return clientResponse.bodyToMono(String.class);
            });

    // Combine the results in a single Mono, than completes when both calls have returned
    // If an error occurs in one of the monos, execution stops immediately.
    // If we want to delay errors and execute all monos, then we can use zipDelayError instead
    Mono<Tuple2<CustomerDTO, String>> zippedCalls = Mono.zip(customerInfo, msisdnStatus);

    //Perform additional actions after the combined mono has returned
    return zippedCalls.flatMap(resultTuple -> {

        // After the calls have completed, generate a random pin
        int pin = 100000 + new Random().nextInt(900000);

        // Save the OTP to local DB, in a reactive manner
        Mono<OTP> otpMono = otpRepository.save(OTP.builder()
                        .customerId(resultTuple.getT1().getAccountId())
                        .pin(pin)
                        .createdOn(ZonedDateTime.now())
                        .status(OTPStatus.ACTIVE)
                        .applicationId(1)
                        .attemptCount(0)
                        .build());

        // External notification service invocation
        Mono<NotificationResultDTO> notificationResultDTOMono = webclient.build()
                .post()
                .uri(notificationServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NotificationRequestForm.builder()
                        .channel("SMS")
                        .msisdn(form.getMsisdn())
                        .message(String.valueOf(pin))
                        .build()))
                .retrieve()
                .bodyToMono(NotificationResultDTO.class);

        // When this operation is complete, the external notification service will be invoked, to send the OTP though the default channel
        // The results are combined in a single Mono
        return otpMono.zipWhen(otp -> notificationResultDTOMono)
                // Return only the result of the first call (DB)
                .map(Tuple2::getT1);

    });
  }

  /**
   * Resend an already generated OTP
   * @param otpId the OTP id
   */
  public Mono<OTP> resend(Long otpId, Boolean sms, Boolean viber) {
    return get(otpId)
            //call
        .flatMap(this::sendToMail);
  }

  /**
   * Validates an OTP and updates its status as {@link OTPStatus#ACTIVE} on success
   *
   * @param otpId the OTP id
   * @param pin the OTP PIN number
   */
  public Mono<String> validate(Long otpId, Integer pin) {
	  return otpRepository.findByIdAndPinAndStatus(otpId, pin, OTPStatus.ACTIVE)
			  .flatMap(otp -> {
					  if (otp.getCreatedOn().isAfter(ZonedDateTime.now().minus(Duration.ofSeconds(120)))) {
						  otp.setStatus(OTPStatus.VERIFIED);
					  } else {
						  otp.setStatus(OTPStatus.EXPIRED);
					  }
					  otp.setAttemptCount(otp.getAttemptCount() + 1);
					  Mono<OTP> saved = otpRepository.save(otp);
					  if (otp.getStatus().equals(OTPStatus.VERIFIED))
					  	return saved;
					  else return Mono.error(new RuntimeException("Expired"));
				  })
			  .switchIfEmpty(Mono.error(new RuntimeException("Not found")))
			  .thenReturn("OK"); // or .doOnSuccess
  }

  private Mono<OTP> sendToMail(OTP otp) {
    // call notification-ms

//    return Mono.error(new RuntimeException("send failed"));
    return Mono.just(otp);
  }

}
