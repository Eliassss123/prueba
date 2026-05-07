package alerts.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI alertasOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Firewall — Microservicio de alertas (firewall-alerta)")
				.version("1.0.0")
				.description("""
					Gestiona **alertas operativas** (tipo, ubicación, severidad, descripción) y persiste en base de datos.

					**Integración con reportes**
					- Tras crear una alerta con `POST /alerts`, el servicio intenta replicar un **reporte estructurado** \
					en `ms-reportes` (`POST .../api/reportes/enviar`) con un DTO compatible.
					- La cadena `location` puede ser coordenadas (`lat,lon`) o texto; ver documentación del mapper.

					**Eureka & Gateway:** `spring.application.name=firewall-alerta`; en el gateway las rutas suelen ser `/alerts/**`.
					""")
				.contact(new Contact()
					.name("Equipo Firewall")
					.email("soporte@firewall.local"))
				.license(new License()
					.name("Uso interno / proyecto académico")
					.url("https://springdoc.org")))
			.servers(List.of(
				new Server().url("http://localhost:8082").description("Instancia local (puerto 8082)"),
				new Server().url("http://localhost:8080").description("API Gateway — prefijo /alerts")));
	}
}
