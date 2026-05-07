package alerts;

import alerts.dto.request.CreateAlertRequest;
import alerts.dto.response.AlertResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/alerts")
@CrossOrigin(origins = "*")
@Tag(
	name = "Alertas",
	description = """
		CRUD de **alertas operativas**. Tras crear una alerta, el servicio intenta **replicar** el evento \
		como reporte en `ms-reportes` (integración HTTP). Consulte el microservicio de reportes para el esquema exacto."""
)
public class AlertController {

	private final AlertService alertService;

	public AlertController(AlertService alertService) {
		this.alertService = alertService;
	}

	@GetMapping
	@Operation(summary = "Listar todas las alertas")
	@ApiResponse(responseCode = "200", description = "Colección actual",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = AlertResponse.class))))
	public ResponseEntity<List<AlertResponse>> getAllAlerts() {
		return ResponseEntity.ok(alertService.getAllAlerts());
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener alerta por ID")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Encontrada",
			content = @Content(schema = @Schema(implementation = AlertResponse.class))),
		@ApiResponse(responseCode = "404", description = "No existe", content = @Content)
	})
	public ResponseEntity<AlertResponse> getAlertById(
		@Parameter(description = "Identificador de la alerta", example = "10")
		@PathVariable Long id) {
		return alertService.getAlertById(id)
			.map(ResponseEntity::ok)
			.orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	@Operation(
		summary = "Crear alerta",
		description = "Persiste la alerta y **notifica** a ms-reportes en segundo plano (errores de red se registran sin fallar la respuesta)."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "201", description = "Creada",
			content = @Content(schema = @Schema(implementation = AlertResponse.class))),
		@ApiResponse(responseCode = "400", description = "Validación fallida", content = @Content)
	})
	public ResponseEntity<AlertResponse> createAlert(@Valid @RequestBody CreateAlertRequest request) {
		AlertResponse created = alertService.createAlert(request);
		return ResponseEntity.status(HttpStatus.CREATED).body(created);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar alerta", description = "No dispara reenvío automático a reportes (solo persistencia local).")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Actualizada",
			content = @Content(schema = @Schema(implementation = AlertResponse.class))),
		@ApiResponse(responseCode = "404", description = "Alerta no encontrada", content = @Content),
		@ApiResponse(responseCode = "400", description = "Cuerpo inválido", content = @Content)
	})
	public ResponseEntity<AlertResponse> updateAlert(
		@Parameter(description = "ID") @PathVariable Long id,
		@Valid @RequestBody CreateAlertRequest request) {
		try {
			AlertResponse updated = alertService.updateAlert(id, request);
			return ResponseEntity.ok(updated);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar alerta")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Eliminada"),
		@ApiResponse(responseCode = "404", description = "No encontrada (según implementación)", content = @Content)
	})
	public ResponseEntity<Void> deleteAlert(
		@Parameter(description = "ID") @PathVariable Long id) {
		alertService.deleteAlert(id);
		return ResponseEntity.noContent().build();
	}
}
