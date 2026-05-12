package com.firewall.geolocalizacion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Registro de monitoreo con enlace a mapa y coordenadas.")
public class ReporteResponseDTO {
	@Schema(example = "7")
	private Long id;

	@Schema(description = "URL generada para abrir la ubicación en servicio de mapas", example = "https://www.google.com/maps?q=-33.4489,-70.6693")
	private String googleMapsUrl;

	@Schema(example = "-33.4489")
	private Double latitud;

	@Schema(example = "-70.6693")
	private Double longitud;

	@Schema(description = "Momento en que se generó o actualizó el registro")
	private LocalDateTime fechaGeneracion;
}