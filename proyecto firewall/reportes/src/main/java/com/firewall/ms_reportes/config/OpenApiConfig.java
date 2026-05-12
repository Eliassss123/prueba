package com.firewall.ms_reportes.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class OpenApiConfig implements WebMvcConfigurer {

	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addRedirectViewController("/swagger-ui.html", "/swagger-ui/index.html");
	}

	@Bean
	public OpenAPI reportesOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Firewall — Microservicio de reportes (ms-reportes)")
				.version("1.0.0")
				.description("""
					Recibe **reportes de incendios y emergencias** enviados por ciudadanos: datos de usuario, \
					descripción, ubicación geográfica y archivos multimedia asociados.

					**Flujos habituales**
					- Crear reporte: `POST /api/reportes/enviar` (IP del cliente se registra automáticamente).
					- El microservicio de **alertas** puede replicar alertas hacia este servicio (mismo cuerpo DTO).
					- Tras el registro en **Eureka**, el acceso vía **API Gateway** suele ser bajo el prefijo `/ms-reportes`.

					**Autenticación:** actualmente no hay OAuth/JWT; proteger en capa de red o añadir seguridad según despliegue.
					""")
				.contact(new Contact()
					.name("Equipo Firewall")
					.email("soporte@firewall.local"))
				.license(new License()
					.name("Uso interno / proyecto académico")
					.url("https://springdoc.org")))
			.servers(List.of(
				new Server().url("http://localhost:8081").description("Instancia local (puerto 8081)"),
				new Server().url("http://localhost:8080/ms-reportes").description("A través del API Gateway (ruta reescrita)")));
	}
}
