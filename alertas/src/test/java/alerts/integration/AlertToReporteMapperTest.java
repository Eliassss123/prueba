package alerts.integration;

import alerts.Alert;
import alerts.integration.dto.ReporteSyncRequest;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AlertToReporteMapperTest {

	@Test
	void parseaCoordenadasConComa() {
		Alert a = new Alert();
		a.setId(5L);
		a.setType("INCENDIO");
		a.setSeverity("ALTA");
		a.setLocation("-33.4000, -70.6000");
		a.setDescription("Humo visible");

		ReporteSyncRequest dto = AlertToReporteMapper.toReporteRequest(a, "1-9");
		assertThat(dto.getUbicacion().getLatitud()).isEqualByComparingTo(new BigDecimal("-33.4000000"));
		assertThat(dto.getUbicacion().getLongitud()).isEqualByComparingTo(new BigDecimal("-70.6000000"));
		assertThat(dto.getDescripcion()).contains("INCENDIO");
		assertThat(dto.getUsuario().getRut()).isEqualTo("1-9");
	}

	@Test
	void ubicacionTextoUsaCoordenadasPorDefecto() {
		Alert a = new Alert();
		a.setId(1L);
		a.setType("PRUEBA");
		a.setSeverity("BAJA");
		a.setLocation("Plaza de Armas, Santiago");

		ReporteSyncRequest dto = AlertToReporteMapper.toReporteRequest(a, "1-9");
		assertThat(dto.getUbicacion().getLatitud()).isEqualByComparingTo(BigDecimal.valueOf(-33.4489));
		assertThat(dto.getUbicacion().getDireccionReferencial()).contains("Plaza");
	}
}
