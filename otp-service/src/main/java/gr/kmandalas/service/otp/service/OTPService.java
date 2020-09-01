package gr.kmandalas.service.otp.service;

import gr.kmandalas.service.otp.dto.CustomerDTO;
import gr.kmandalas.service.otp.dto.NotificationRequestForm;
import gr.kmandalas.service.otp.dto.NotificationResultDTO;
import gr.kmandalas.service.otp.dto.SendForm;
import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.enumeration.OTPStatus;
import gr.kmandalas.service.otp.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
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

import static org.springframework.data.mapping.Alias.ofNullable;

@Service
@RequiredArgsConstructor
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
        .switchIfEmpty(Mono.error(new RuntimeException("OTP not found")));
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

    // Make 2 parallel calls and combine the results in a single Mono
    return Mono.zip(
            // 1st call to customer-service using service discovery, to retrieve customer related info
            loadbalanced.build()
                    .get()
                    .uri(customerURI)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(CustomerDTO.class),
            // 2nd call to external service, to check that the MSISDN is valid
            webclient.build()
                    .get()
                    .uri(numberInfoURI)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class))
            .flatMap(resultTuple -> {

              // After the calls have completed, generate a random pin
              int pin = 100000 + new Random().nextInt(900000);

              // Save the OTP to local DB
              return otpRepository
                      .save(OTP.builder()
                              .customerId(resultTuple.getT1().getAccountId())
                              .pin(pin)
                              .createdOn(ZonedDateTime.now())
                              .status(OTPStatus.UNUSED)
                              .build())
                      // When this operation is complete, the external notification service will be invoked, to send the OTP though the default channel
                      // The results are combined in a single Mono
                      .zipWhen(otp -> webclient.build()
                              .post()
                              .uri(notificationServiceUrl)
                              .accept(MediaType.APPLICATION_JSON)
                              .body(BodyInserters.fromValue(NotificationRequestForm.builder()
                                      .channel("SMS")
                                      .msisdn(form.getMsisdn())
                                      .message(String.valueOf(pin))
                                      .build()))
                              .retrieve()
                              .bodyToMono(NotificationResultDTO.class))
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
   * Validate an OTP
   * @param otpId the OTP id
   * @param pin the OTP PIN number
   */
  public Mono<String> validate(Long otpId, Integer pin) {
      return get(otpId)
          .map(currentOTP -> {
            if (currentOTP.getPin().equals(pin)
                && currentOTP.getCreatedOn().isAfter(ZonedDateTime.now().minus(Duration.ofSeconds(30))))
              return "Valid!";
            else
              return "Invalid...";
          });
  }

  private Mono<OTP> sendToMail(OTP otp) {
    // call notification-ms

//    return Mono.error(new RuntimeException("send failed"));
    return Mono.just(otp);
  }

}
