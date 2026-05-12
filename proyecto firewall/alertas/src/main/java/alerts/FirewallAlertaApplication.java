package alerts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

/**
 * Aplicación principal del módulo de alertas del firewall.
 * Integra alertas con el microservicio de reportes vía eventos.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FirewallAlertaApplication {

	public static void main(String[] args) {
		SpringApplication.run(FirewallAlertaApplication.class, args);
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
