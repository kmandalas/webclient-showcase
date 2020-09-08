package gr.kmandalas.service.otp;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@ActiveProfiles("test")
public class LoadBalancerTestConfiguration {

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
		return Flux.just(Collections.singletonList(
				new DefaultServiceInstance(serviceId + "1", serviceId, "localhost", 7999, false)));
	}

}

