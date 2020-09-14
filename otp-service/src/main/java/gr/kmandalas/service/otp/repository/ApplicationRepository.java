package gr.kmandalas.service.otp.repository;

import gr.kmandalas.service.otp.entity.Application;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ApplicationRepository extends ReactiveCrudRepository<Application, String> {

}
