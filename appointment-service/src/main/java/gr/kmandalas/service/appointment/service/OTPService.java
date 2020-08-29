package gr.kmandalas.service.appointment.service;

import gr.kmandalas.service.appointment.dto.SendForm;
import gr.kmandalas.service.appointment.entity.OTP;
import gr.kmandalas.service.appointment.enumeration.OTPStatus;
import gr.kmandalas.service.appointment.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Random;

import static org.springframework.data.mapping.Alias.ofNullable;

@Service
@RequiredArgsConstructor
public class OTPService {

  private final OTPRepository otpRepository;

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
    return otpRepository
        .save(OTP.builder()
            .customerId(form.getCustomerId())
            .pin(100000 + new Random().nextInt(900000))
            .createdOn(ZonedDateTime.now())
            .status(OTPStatus.UNUSED)
            .build())
        .flatMap(this::sendToMail);
  }

  /**
   * Resend an already generated OTP
   * @param otpId the OTP id
   */
  public Mono<OTP> resend(Long otpId) {
    return get(otpId)
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
