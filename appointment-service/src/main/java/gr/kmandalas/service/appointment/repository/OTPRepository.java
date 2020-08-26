package gr.kmandalas.service.appointment.repository;

import gr.kmandalas.service.appointment.entity.OTP;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OTPRepository extends ReactiveCrudRepository<OTP, Long> {

  Mono<OTP> findByCustomerId(String customerId);
}
