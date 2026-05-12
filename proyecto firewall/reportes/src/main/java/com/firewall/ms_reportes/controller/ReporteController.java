package com.firewall.ms_reportes.controller;

import com.firewall.ms_reportes.dto.request.CreateReporteRequestDTO;
import com.firewall.ms_reportes.dto.response.ReporteResponseDTO;
import com.firewall.ms_reportes.service.ReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@Tag(
	name = "Reportes",
	description = """
		Alta, consulta, filtrado y eliminación de **reportes de emergencia** enviados por la ciudadanía. \
		El endpoint `enviar` registra la **IP de origen** del cliente."""
)
public class ReporteController {

	@Autowired
	private ReporteService reporteService;

	@PostMapping("/enviar")
	@Operation(
		summary = "Enviar nuevo reporte",
		description = """
			Crea un reporte completo. La IP visible para el servidor (cabeceras proxy, si existen) \
			se guarda con el usuario según la lógica de negocio."""
	)
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Reporte creado correctamente",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "400", description = "Validación fallida (campos obligatorios, formatos)", content = @Content)
	})
	public ReporteResponseDTO crearReporte(
		@Valid @RequestBody CreateReporteRequestDTO request,
		HttpServletRequest httpRequest) {
		String ipCliente = httpRequest.getRemoteAddr();
		return reporteService.crearReporte(request, ipCliente);
	}

	@GetMapping
	@Operation(summary = "Listar todos los reportes", description = "Devuelve la colección completa ordenada según implementación del servicio.")
	@ApiResponse(responseCode = "200", description = "Lista obtenida",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReporteResponseDTO.class))))
	public List<ReporteResponseDTO> listarReportes() {
		return reporteService.listarReportes();
	}

	@GetMapping("/usuario/{rut}")
	@Operation(summary = "Reportes por RUT", description = "Filtra por identificador del usuario que reportó.")
	@ApiResponse(responseCode = "200", description = "Lista (puede estar vacía)",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReporteResponseDTO.class))))
	public List<ReporteResponseDTO> obtenerReportesPorUsuario(
		@Parameter(description = "RUT u otro identificador almacenado", example = "12.345.678-9")
		@PathVariable String rut) {
		return reporteService.obtenerReportesPorUsuario(rut);
	}

	@GetMapping("/estado/{estado}")
	@Operation(summary = "Reportes por estado", description = "Filtra por campo de estado del flujo (ej. Enviado, cerrado).")
	@ApiResponse(responseCode = "200", description = "Lista (puede estar vacía)",
		content = @Content(array = @ArraySchema(schema = @Schema(implementation = ReporteResponseDTO.class))))
	public List<ReporteResponseDTO> obtenerReportesPorEstado(
		@Parameter(description = "Valor de estado", example = "Enviado")
		@PathVariable String estado) {
		return reporteService.obtenerReportesPorEstado(estado);
	}

	@GetMapping("/{id}")
	@Operation(summary = "Obtener reporte por ID")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Encontrado",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "404", description = "No existe un reporte con ese id", content = @Content)
	})
	public ResponseEntity<ReporteResponseDTO> obtenerReporte(
		@Parameter(description = "Identificador numérico", example = "1")
		@PathVariable Long id) {
		try {
			ReporteResponseDTO reporte = reporteService.obtenerReporte(id);
			return ResponseEntity.ok(reporte);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@PutMapping("/{id}")
	@Operation(summary = "Actualizar reporte", description = "Reemplaza datos del reporte existente; actualiza IP si aplica.")
	@ApiResponses({
		@ApiResponse(responseCode = "200", description = "Actualizado",
			content = @Content(schema = @Schema(implementation = ReporteResponseDTO.class))),
		@ApiResponse(responseCode = "404", description = "Reporte no encontrado", content = @Content),
		@ApiResponse(responseCode = "400", description = "Cuerpo inválido", content = @Content)
	})
	public ResponseEntity<ReporteResponseDTO> actualizarReporte(
		@Parameter(description = "ID del reporte") @PathVariable Long id,
		@Valid @RequestBody CreateReporteRequestDTO request,
		HttpServletRequest httpRequest) {
		try {
			ReporteResponseDTO updated = reporteService.actualizarReporte(id, request, httpRequest.getRemoteAddr());
			return ResponseEntity.ok(updated);
		} catch (RuntimeException e) {
			return ResponseEntity.notFound().build();
		}
	}

	@DeleteMapping("/{id}")
	@Operation(summary = "Eliminar reporte")
	@ApiResponses({
		@ApiResponse(responseCode = "204", description = "Eliminado (sin cuerpo)"),
		@ApiResponse(responseCode = "404", description = "ID inexistente — puede devolver 204 según implementación", content = @Content)
	})
	public ResponseEntity<Void> eliminarReporte(
		@Parameter(description = "ID del reporte") @PathVariable Long id) {
		reporteService.eliminarReporte(id);
		return ResponseEntity.noContent().build();
	}
}
