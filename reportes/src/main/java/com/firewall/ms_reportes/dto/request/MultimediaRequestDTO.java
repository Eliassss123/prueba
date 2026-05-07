package com.firewall.ms_reportes.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Metadatos de un archivo multimedia ya alojado (URL + tipo MIME o categoría).")
public class MultimediaRequestDTO {
	@NotBlank(message = "La URL de S3 es obligatoria")
	@Schema(description = "URL del objeto (p. ej. bucket S3 u otro almacenamiento).", example = "https://cdn.ejemplo.cl/fotos/incendio-001.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
	private String urlS3;

	@NotBlank(message = "El tipo de archivo es obligatorio")
	@Schema(description = "Tipo o extensión lógica", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
	private String tipoArchivo;
}