package gr.kmandalas.service.otp;

import lombok.Getter;
import lombok.Setter;

import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
public class BaseControllerIT {

	@TestConfiguration
	public class SayHelloConfiguration {

		@Bean
		@Primary
		ServiceInstanceListSupplier serviceInstanceListSupplier() {
			return new DemoServiceInstanceListSuppler("customer-service");
		}

	}

	class DemoServiceInstanceListSuppler implements ServiceInstanceListSupplier {

		private final String serviceId;

		DemoServiceInstanceListSuppler(String serviceId) {
			this.serviceId = serviceId;
		}

		@Override
		public String getServiceId() {
			return serviceId;
		}

		@Override
		public Flux<List<ServiceInstance>> get() {
			return Flux.just(Arrays
					.asList(new DefaultServiceInstance(serviceId + "1", serviceId, "localhost", 7999, false)));
		}
	}

}
