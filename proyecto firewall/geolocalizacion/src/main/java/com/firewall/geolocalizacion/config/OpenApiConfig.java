package com.firewall.geolocalizacion.config;

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
	public OpenAPI monitoreoOpenAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("Firewall — Monitoreo y geolocalización (monitoreo-service)")
				.version("1.0.0")
				.description("""
					Expone operaciones de **monitoreo** basadas en **coordenadas** y generación de **enlace a mapas** \
					(p. ej. Google Maps) asociado a un reporte de posición.

					**Uso**
					- Listar o consultar entradas de monitoreo; crear/actualizar con latitud, longitud y descripción.
					- Pensado para **rutas de evacuación / llegada** complementando el resto de la plataforma.

					**Eureka:** `spring.application.name=monitoreo-service`. Vía gateway: prefijo `/monitoreo` con reescritura de ruta.
					""")
				.contact(new Contact()
					.name("Equipo Firewall")
					.email("soporte@firewall.local"))
				.license(new License()
					.name("Uso interno / proyecto académico")
					.url("https://springdoc.org")))
			.servers(List.of(
				new Server().url("http://localhost:8083").description("Instancia local (puerto 8083)"),
				new Server().url("http://localhost:8080").description("API Gateway — prefijo /monitoreo")));
	}
}
