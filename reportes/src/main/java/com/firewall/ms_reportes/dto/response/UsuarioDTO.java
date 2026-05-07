package com.firewall.ms_reportes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Usuario asociado al reporte (persistido).")
public class UsuarioDTO {
	@Schema(example = "101")
	private Long id;

	@Schema(example = "12.345.678-9")
	private String rut;

	@Schema(description = "IP desde la que se envió el reporte (si aplica)", example = "192.168.1.10")
	private String direccionIp;
}