package alerts.integration.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Cuerpo JSON compatible con {@code CreateReporteRequestDTO} del microservicio reportes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReporteSyncRequest {

	private Usuario usuario;
	private String descripcion;
	private Ubicacion ubicacion;
	private List<Object> multimedia;

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Usuario {
		private String rut;
	}

	@Data
	@Builder
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Ubicacion {
		private BigDecimal latitud;
		private BigDecimal longitud;
		private String direccionReferencial;
	}
}
