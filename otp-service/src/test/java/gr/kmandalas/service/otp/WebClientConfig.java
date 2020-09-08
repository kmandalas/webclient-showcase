package gr.kmandalas.service.otp;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@LoadBalancerClient(name = "lb-test", configuration = LoadBalancerTestConfiguration.class)
@ActiveProfiles("test")
public class WebClientConfig {

	@Bean
	@Primary
	WebClient.Builder webClient() {
		return WebClient.builder()
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
	}

	@Bean
	@LoadBalanced
	WebClient.Builder loadBalanced() {
		return WebClient.builder()
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
	}

}
