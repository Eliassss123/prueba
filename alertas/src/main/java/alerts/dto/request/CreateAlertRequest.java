package alerts.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(
	name = "CreateAlertRequest",
	description = """
		Datos para crear o actualizar una alerta.

		**Ubicación (`location`):** puede ser texto libre o coordenadas separadas por coma o punto y coma, \
		p. ej. `-33.45,-70.65`. Si no son coordenadas parseables, se usarán valores por defecto al replicar en reportes.
		"""
)
public class CreateAlertRequest {
	@NotBlank(message = "El tipo de alerta es obligatorio")
	@Schema(description = "Clasificación del evento", example = "INCENDIO_FORESTAL", requiredMode = Schema.RequiredMode.REQUIRED)
	private String type;

	@NotBlank(message = "La ubicación es obligatoria")
	@Schema(
		description = "Ubicación textual o coordenadas `lat,lon`",
		example = "-33.4489, -70.6693",
		requiredMode = Schema.RequiredMode.REQUIRED
	)
	private String location;

	@NotBlank(message = "La severidad es obligatoria")
	@Schema(description = "Nivel de severidad operativa", example = "CRITICA", requiredMode = Schema.RequiredMode.REQUIRED)
	private String severity;

	@Schema(description = "Detalle adicional opcional", example = "Viviendas amenazadas al sur del cerro")
	private String description;
}