package com.firewall.ms_reportes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Ubicación devuelta por el API.")
public class UbicacionDTO {
	@Schema(example = "-33.4489")
	private BigDecimal latitud;

	@Schema(example = "-70.6693")
	private BigDecimal longitud;

	@Schema(description = "Texto de referencia")
	private String direccionReferencial;
}