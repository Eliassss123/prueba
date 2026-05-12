package com.firewall.ms_reportes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Adjunto asociado al reporte.")
public class MultimediaDTO {
	@Schema(example = "9001")
	private Long id;

	@Schema(description = "URL del recurso")
	private String urlS3;

	@Schema(example = "image/png")
	private String tipoArchivo;
}