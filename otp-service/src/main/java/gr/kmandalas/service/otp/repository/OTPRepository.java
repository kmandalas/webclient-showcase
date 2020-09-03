package gr.kmandalas.service.otp.repository;

import gr.kmandalas.service.otp.entity.OTP;
import gr.kmandalas.service.otp.enumeration.OTPStatus;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OTPRepository extends ReactiveCrudRepository<OTP, Long> {

  Flux<OTP> findByCustomerId(Long customerId);

  Mono<OTP> findByIdAndPinAndStatus(Long otpId, Integer pin, OTPStatus status);
}
