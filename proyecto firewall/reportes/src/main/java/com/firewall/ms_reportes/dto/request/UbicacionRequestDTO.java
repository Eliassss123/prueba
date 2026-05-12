package com.firewall.ms_reportes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "Ubicación geográfica del incidente (WGS84).")
public class UbicacionRequestDTO {
	@NotNull(message = "La latitud es obligatoria")
	@Schema(description = "Latitud", example = "-33.4489", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal latitud;

	@NotNull(message = "La longitud es obligatoria")
	@Schema(description = "Longitud", example = "-70.6693", requiredMode = Schema.RequiredMode.REQUIRED)
	private BigDecimal longitud;

	@Schema(description = "Referencia humana (calle, barrio, lugar conocido).", example = "Esquina Av. Matta con Gran Avenida")
	private String direccionReferencial;
}