package alerts.integration;

import alerts.Alert;
import alerts.integration.dto.ReporteSyncRequest;

import java.math.BigDecimal;

/**
 * Mapea alertas del sistema a peticiones de reporte para ser sincronizadas.
 */
public class AlertToReporteMapper {

	private static final BigDecimal DEFAULT_LAT = new BigDecimal("-33.4489");
	private static final BigDecimal DEFAULT_LON = new BigDecimal("-70.6693");

	public static ReporteSyncRequest toReporteRequest(Alert alert, String rutSistema) {
		ReporteSyncRequest request = new ReporteSyncRequest();

		// Usuario
		ReporteSyncRequest.Usuario usuario = new ReporteSyncRequest.Usuario();
		usuario.setRut(rutSistema);
		request.setUsuario(usuario);

		// Descripción: Combina tipo, severidad y descripción
		StringBuilder desc = new StringBuilder();
		if (alert.getType() != null) {
			desc.append(alert.getType());
		}
		if (alert.getSeverity() != null) {
			desc.append(" [").append(alert.getSeverity()).append("]");
		}
		if (alert.getDescription() != null) {
			desc.append(": ").append(alert.getDescription());
		}
		request.setDescripcion(desc.toString());

		// Ubicación
		ReporteSyncRequest.Ubicacion ubicacion = parseLocation(alert.getLocation());
		request.setUbicacion(ubicacion);

		return request;
	}

	private static ReporteSyncRequest.Ubicacion parseLocation(String location) {
		ReporteSyncRequest.Ubicacion ubicacion = new ReporteSyncRequest.Ubicacion();

		if (location == null || location.isBlank()) {
			ubicacion.setLatitud(DEFAULT_LAT);
			ubicacion.setLongitud(DEFAULT_LON);
			return ubicacion;
		}

		// Intenta parsear si es formato numérico: "lat, lon"
		if (location.contains(",")) {
			String[] parts = location.split(",");
			if (parts.length == 2) {
				try {
					BigDecimal lat = new BigDecimal(parts[0].trim()).setScale(7, java.math.RoundingMode.HALF_UP);
					BigDecimal lon = new BigDecimal(parts[1].trim()).setScale(7, java.math.RoundingMode.HALF_UP);
					ubicacion.setLatitud(lat);
					ubicacion.setLongitud(lon);
					return ubicacion;
				} catch (NumberFormatException e) {
					// Si falla, usar como referencial y coords por defecto
					ubicacion.setDireccionReferencial(location);
					ubicacion.setLatitud(DEFAULT_LAT);
					ubicacion.setLongitud(DEFAULT_LON);
					return ubicacion;
				}
			}
		}

		// Si es texto puro, usarlo como referencia
		ubicacion.setDireccionReferencial(location);
		ubicacion.setLatitud(DEFAULT_LAT);
		ubicacion.setLongitud(DEFAULT_LON);
		return ubicacion;
	}
}
