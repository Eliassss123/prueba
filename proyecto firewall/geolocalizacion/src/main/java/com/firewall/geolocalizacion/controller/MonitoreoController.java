package com.firewall.geolocalizacion.controller;

import com.firewall.geolocalizacion.dto.CoordenadaRequest;
import com.firewall.geolocalizacion.dto.response.ReporteResponseDTO;
import com.firewall.geolocalizacion.service.MonitoreoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitoreo")
@Tag(
	name = "Monitoreo",
	description = """
		API de **geolocalización / monitoreo**: crea puntos con coordenadas, genera URL de mapas y permite \
		consultar o actualizar registros. Útil para visualizar **rutas** o posiciones operativas."""
)
public class MonitoreoController {

	private final MonitoreoService service;

	public MonitoreoController(MonitoreoService service) {
		this.service = service;
	}

	@GetMapping
	@Operation(summary = "Listar registros de monitoreo", description = "Devuelve todos los registros con coordenadas y enlace generado.")
	@ApiResponse(responseCode = "200", description = "Lista obtenida",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReporteResponseDTO.class))))
	public List<ReporteResponseDTO> obtenerReportes() {
		return service.obtenerReportes();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener registro por ID")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Encontrado",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "500", description = "Registro inexistente — puede lanzar error de negocio", content = @Content)
	})
	public ReporteResponseDTO obtenerReporte(
		@Parameter(description = "Identificador del registro", example = "3")
		@PathVariable Long id) {
		return service.obtenerReporte(id);
	}

	@PostMapping
	@Operation(
		summary = "Crear registro desde coordenadas",
		description = "Persiste coordenadas y descripción; genera URL de mapas y fecha de generación."
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Creado y devuelto",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "Validación de entrada", content = @Content)
	})
	public ReporteResponseDTO crearReporte(@Valid @RequestBody CoordenadaRequest datos) {
		return service.procesarDatos(datos);
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar registro", description = "Actualiza coordenadas y enlaces asociados al ID indicado.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Actualizado",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "Validación", content = @Content),
		@ApiResponse(responseCode = "500", description = "ID no encontrado", content = @Content)
	})
	public ReporteResponseDTO actualizarReporte(
		@Parameter(description = "ID del registro") @PathVariable Long id,
		@Valid @RequestBody CoordenadaRequest datos) {
		return service.actualizarReporte(id, datos);
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar registro")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Eliminado (sin cuerpo en implementación actual)"),
		@ApiResponse(responseCode = "404", description = "Si el ID no existe — comportamiento según capa de datos", content = @Content)
	})
	public void eliminarReporte(
		@Parameter(description = "ID") @PathVariable Long id) {
		service.eliminarReporte(id);
	}
}
