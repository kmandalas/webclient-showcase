package gr.kmandalas.service.otp.repository;

import gr.kmandalas.service.otp.entity.OTP;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface OTPRepository extends ReactiveCrudRepository<OTP, Long> {

  Flux<OTP> findByCustomerId(Long customerId);
}
