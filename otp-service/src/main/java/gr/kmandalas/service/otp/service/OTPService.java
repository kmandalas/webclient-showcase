package gr.kmandalas.service.otp.service;

import gr.kmandalas.service.otp.dto.CustomerDTO;
import gr.kmandalas.service.otp.dto.NotificationRequestForm;
import gr.kmandalas.service.otp.dto.NotificationResultDTO;
import gr.kmandalas.service.otp.dto.SendForm;
import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.enumeration.Channel;
import gr.kmandalas.service.otp.enumeration.FaultReason;
import gr.kmandalas.service.otp.enumeration.OTPStatus;
import gr.kmandalas.service.otp.exception.OTPException;
import gr.kmandalas.service.otp.repository.ApplicationRepository;
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
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.data.mapping.Alias.ofNullable;

@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {

  private final OTPRepository otpRepository;

  private final ApplicationRepository applicationRepository;

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
        .switchIfEmpty(Mono.error(new OTPException("OTP not found", FaultReason.NOT_FOUND)));
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
                    clientResponse -> Mono.error(new OTPException("Error retrieving Customer", FaultReason.CUSTOMER_ERROR)))
            .bodyToMono(CustomerDTO.class);

    // 2nd call to external service, to check that the MSISDN is valid
    Mono<String> msisdnStatus = webclient.build()
            .get()
            .uri(numberInfoURI)
            .exchange()
            .flatMap(clientResponse -> {
                if (!clientResponse.statusCode().is2xxSuccessful())
                    return Mono.error(new OTPException("Error retrieving msisdn status", FaultReason.NUMBER_INFORMATION_ERROR));
                return clientResponse.bodyToMono(String.class);
            });

    // Combine the results in a single Mono, that completes when both calls have returned.
    // If an error occurs in one of the Monos, execution stops immediately.
    // If we want to delay errors and execute all Monos, then we can use zipDelayError instead
    Mono<Tuple2<CustomerDTO, String>> zippedCalls = Mono.zip(customerInfo, msisdnStatus);

    // Perform additional actions after the combined mono has returned
    return zippedCalls.flatMap(resultTuple -> {

        // After the calls have completed, generate a random pin
        int pin = 100000 + new Random().nextInt(900000);

        // Save the OTP to local DB, in a reactive manner
        Mono<OTP> otpMono = otpRepository.save(OTP.builder()
                        .customerId(resultTuple.getT1().getAccountId())
                        .pin(pin)
                        .createdOn(ZonedDateTime.now())
						.expires(ZonedDateTime.now().plus(Duration.ofMinutes(1)))
                        .status(OTPStatus.ACTIVE)
                        .applicationId("PPR")
                        .attemptCount(0)
                        .build());

        // External notification service invocation
        Mono<NotificationResultDTO> notificationResultDTOMono = webclient.build()
                .post()
                .uri(notificationServiceUrl)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NotificationRequestForm.builder()
                        .channel(Channel.AUTO.name())
                        .msisdn(form.getMsisdn())
                        .message(String.valueOf(pin))
                        .build()))
                .retrieve()
                .bodyToMono(NotificationResultDTO.class);

        // When this operation is complete, the external notification service will be invoked, to send the OTP though the default channel.
        // The results are combined in a single Mono:
        return otpMono.zipWhen(otp -> notificationResultDTOMono)
                // Return only the result of the first call (DB)
                .map(Tuple2::getT1);
    });
  }

  /**
   * Resend an already generated OTP. If a mail is given then send additional notification with parallel call.
   *
   * @param otpId the OTP id
   */
  public Mono<OTP> resend(Long otpId, String channel, String mail) {
    return Mono.error(UnsupportedOperationException::new); // TODO
  }

  /**
   * Validates an OTP and updates its status as {@link OTPStatus#ACTIVE} on success
   *
   * @param otpId the OTP id
   * @param pin the OTP PIN number
   */
  public Mono<OTP> validate(Long otpId, Integer pin) {
  	  AtomicReference<FaultReason> faultReason = new AtomicReference<>();

	  return otpRepository.findById(otpId)
			  .switchIfEmpty(Mono.error(new OTPException("Error validating OTP", FaultReason.NOT_FOUND)))
			  .zipWhen(otp -> applicationRepository.findById(otp.getApplicationId()))
			  .flatMap(Tuple2 -> {
			  	  var otp = Tuple2.getT1();
			  	  var app = Tuple2.getT2();

				  // FaultReason faultReason = null;
			  	  if (otp.getAttemptCount() > app.getAttemptsAllowed()) {
					 otp.setStatus(OTPStatus.TOO_MANY_ATTEMPTS);
					 faultReason.set(FaultReason.TOO_MANY_ATTEMPTS);
				  } else if (!otp.getPin().equals(pin) ) {
					  faultReason.set(FaultReason.INVALID_PIN);
				  } else if (!otp.getStatus().equals(OTPStatus.ACTIVE)) {
					  faultReason.set(FaultReason.INVALID_STATUS);
				  } else if (otp.getExpires().isBefore(ZonedDateTime.now())) {
					  otp.setStatus(OTPStatus.EXPIRED);
					  faultReason.set(FaultReason.EXPIRED);
				  } else {
					  otp.setStatus(OTPStatus.VERIFIED);
				  }

			  	  if (!otp.getStatus().equals(OTPStatus.TOO_MANY_ATTEMPTS))
				  	otp.setAttemptCount(otp.getAttemptCount() + 1);

				  if (otp.getStatus().equals(OTPStatus.VERIFIED))
					  return otpRepository.save(otp);
				  else {
					  return Mono.error(new OTPException("Error validating OTP", faultReason.get(), otp));
				  }
			  })
			  .doOnError(throwable ->
					  otpRepository.save(((OTPException)throwable).getOtp())
							  .subscribe()
			  );
  }

}
