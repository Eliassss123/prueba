package com.firewall.ms_reportes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
@Schema(
	name = "CreateReporteRequest",
	description = "Cuerpo para **crear** o **actualizar** un reporte: quién reporta, qué ocurrió, dónde y pruebas multimedia."
)
public class CreateReporteRequestDTO {
	@NotNull(message = "El usuario es obligatorio")
	@Schema(description = "Persona que envía el reporte (identificación civil).", requiredMode = Schema.RequiredMode.REQUIRED)
	private UsuarioRequestDTO usuario;

	@NotBlank(message = "La descripción es obligatoria")
	@Schema(
		description = "Relato del incidente (texto libre).",
		example = "Columna de humo visible desde Av. Principal",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	private String descripcion;

	@NotNull(message = "La ubicación es obligatoria")
	@Schema(description = "Coordenadas y referencia de ubicación.", requiredMode = Schema.RequiredMode.REQUIRED)
	private UbicacionRequestDTO ubicacion;

	@Schema(description = "Fotos u otros adjuntos ya subidos a almacenamiento (URLs). Opcional.")
	private List<MultimediaRequestDTO> multimedia;
}