package gr.kmandalas.service.appointment.service;

import gr.kmandalas.service.appointment.entity.OTP;
import gr.kmandalas.service.appointment.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OTPService {

  private final OTPRepository otpRepository;

  /**
   * Get the already generated OTP of a customer
   */
  public Mono<OTP> get(String customerId) {
    return otpRepository.findByCustomerId(customerId)
        .switchIfEmpty(Mono.error(new RuntimeException("OTP not found")));
  }

  /**
   * Generate an OTP for a customer
   */
  public Mono<OTP> generate(String customerId) {
    return otpRepository.findByCustomerId(customerId)
        .switchIfEmpty(
            otpRepository.save(OTP.builder()
                .customerId(customerId)
                .value(100000 + new Random().nextInt(900000))
                .createdOn(LocalDateTime.now())
                .status("new")
                .build())
        );
  }

  /**
   * Get the already generated OTP of a customer
   */
  public Mono<String> validate(Integer providedOTP, String customerId) {
      return get(customerId).map(currentOTP -> {
        if (currentOTP.getValue().equals(providedOTP)
            && currentOTP.getCreatedOn().isAfter(LocalDateTime.now().minus(Duration.ofSeconds(30))))
          return "Valid!";
        else
          return "Invalid...";
      });
  }

}
