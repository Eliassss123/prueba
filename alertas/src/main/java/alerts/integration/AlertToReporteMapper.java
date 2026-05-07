package alerts.integration;

import alerts.Alert;
import alerts.integration.dto.ReporteSyncRequest;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convierte una alerta persistida en el payload esperado por ms-reportes ({@code /api/reportes/enviar}).
 */
public final class AlertToReporteMapper {

	private static final Pattern COORD_PAIR = Pattern.compile(
		"^\\s*(-?\\d+(?:\\.\\d+)?)\\s*[,;\\s]\\s*(-?\\d+(?:\\.\\d+)?)\\s*$"
	);

	/** Referencia aproximada (Santiago) si la ubicación no trae coordenadas parseables. */
	private static final BigDecimal DEFAULT_LAT = new BigDecimal("-33.4489");
	private static final BigDecimal DEFAULT_LON = new BigDecimal("-70.6693");

	private AlertToReporteMapper() {
	}

	public static ReporteSyncRequest toReporteRequest(Alert alert, String rutSistema) {
		String location = alert.getLocation() != null ? alert.getLocation().trim() : "";
		BigDecimal lat = DEFAULT_LAT;
		BigDecimal lon = DEFAULT_LON;
		String direccionRef = location;

		Matcher m = COORD_PAIR.matcher(location);
		if (m.matches()) {
			lat = new BigDecimal(m.group(1)).setScale(7, RoundingMode.HALF_UP);
			lon = new BigDecimal(m.group(2)).setScale(7, RoundingMode.HALF_UP);
		}

		String descripcion = buildDescripcion(alert);

		return ReporteSyncRequest.builder()
			.usuario(ReporteSyncRequest.Usuario.builder().rut(rutSistema).build())
			.descripcion(descripcion)
			.ubicacion(ReporteSyncRequest.Ubicacion.builder()
				.latitud(lat)
				.longitud(lon)
				.direccionReferencial(StringUtils.hasText(direccionRef) ? direccionRef : null)
				.build())
			.multimedia(null)
			.build();
	}

	private static String buildDescripcion(Alert alert) {
		StringBuilder sb = new StringBuilder();
		if (alert.getId() != null) {
			sb.append("[ALERTA #").append(alert.getId()).append("] ");
		} else {
			sb.append("[ALERTA] ");
		}
		sb.append(alert.getType() != null ? alert.getType() : "DESCONOCIDO");
		sb.append(" | severidad ").append(alert.getSeverity() != null ? alert.getSeverity() : "-");
		if (StringUtils.hasText(alert.getDescription())) {
			sb.append("\n").append(alert.getDescription());
		}
		return sb.toString();
	}
}
