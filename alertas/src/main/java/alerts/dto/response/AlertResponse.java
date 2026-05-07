package alerts.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Alerta persistida devuelta por la API.")
public class AlertResponse {
	@Schema(example = "15")
	private Long id;

	@Schema(description = "Momento de creación del registro")
	private LocalDateTime timestamp;

	@Schema(example = "INCENDIO_STRUCTURAL")
	private String type;

	@Schema(description = "Ubicación tal como se informó")
	private String location;

	@Schema(example = "ALTA")
	private String severity;

	@Schema(description = "Descripción libre")
	private String description;
}