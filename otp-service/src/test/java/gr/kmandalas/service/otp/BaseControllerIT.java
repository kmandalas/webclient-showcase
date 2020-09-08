package gr.kmandalas.service.otp;

import lombok.Getter;
import lombok.Setter;

import org.junit.jupiter.api.TestInstance;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;

import org.springframework.test.context.ActiveProfiles;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@TestInstance(PER_CLASS)
@ActiveProfiles("test")
public class BaseControllerIT {

	@TestConfiguration
	@Getter
	@Setter
	protected static class TestConfig {

		@Bean
		public ServiceInstanceListSupplier discoveryClientServiceInstanceListSupplier() {

			return new ServiceInstanceListSupplier() {

				@Override
				public String getServiceId() {
					return "customer-service";
				}

				@Override
				public Flux<List<ServiceInstance>> get() {

					ServiceInstance instance1 = new ServiceInstance() {

						@Override
						public String getServiceId() {
							return "customer-service";
						}

						@Override
						public String getHost() {
							return "localhost";
						}

						@Override
						public int getPort() {
							return 7999;
						}

						@Override
						public boolean isSecure() {
							return false;
						}

						@Override
						public URI getUri() {
							return URI.create("http://localhost:7999");
						}

						@Override
						public Map<String, String> getMetadata() {
							return null;
						}
					};

					Flux<ServiceInstance> serviceInstances = Flux
							.defer(() -> Flux.fromIterable(List.of(instance1)))
							.subscribeOn(Schedulers.boundedElastic());
					return serviceInstances.collectList().flux();
				}
			};
		}
	}

}
