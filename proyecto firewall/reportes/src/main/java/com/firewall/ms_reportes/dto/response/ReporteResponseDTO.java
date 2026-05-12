package com.firewall.ms_reportes.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Reporte persistido con usuario, ubicación, estado y multimedia asociada.")
public class ReporteResponseDTO {
	@Schema(description = "Identificador interno del reporte", example = "42")
	private Long id;

	@Schema(description = "Usuario que originó el reporte")
	private UsuarioDTO usuario;

	@Schema(description = "Texto descriptivo del incidente")
	private String descripcion;

	@Schema(description = "Marca de tiempo de creación o última actualización relevante", example = "2026-05-04T14:30:00")
	private LocalDateTime fechaHora;

	@Schema(description = "Estado del flujo (ej. Enviado, En revisión)", example = "Enviado")
	private String estado;

	@Schema(description = "Ubicación almacenada")
	private UbicacionDTO ubicacion;

	@Schema(description = "Lista de adjuntos")
	private List<MultimediaDTO> multimedia;
}