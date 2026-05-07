package com.firewall.ms_reportes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Identificación del usuario que reporta.")
public class UsuarioRequestDTO {
	@NotBlank(message = "El RUT es obligatorio")
	@Schema(description = "RUT u otro identificador civil", example = "12.345.678-9", requiredMode = Schema.RequiredMode.REQUIRED)
	private String rut;
}