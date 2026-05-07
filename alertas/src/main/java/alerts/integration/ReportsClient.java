package alerts.integration;

import alerts.Alert;
import alerts.integration.dto.ReporteSyncRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Envía alertas al microservicio de reportes como {@link ReporteSyncRequest}.
 */
@Component
public class ReportsClient {

	private static final Logger log = LoggerFactory.getLogger(ReportsClient.class);

	private final RestTemplate restTemplate;

	@Value("${reports.service.url:http://localhost:8081/api/reportes/enviar}")
	private String reportsServiceUrl;

	@Value("${alerts.sync.reporte.rut:00000000-0}")
	private String rutSistema;

	public ReportsClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public void enviarReporteDesdeAlerta(Alert alert) {
		ReporteSyncRequest body = AlertToReporteMapper.toReporteRequest(alert, rutSistema);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ReporteSyncRequest> entity = new HttpEntity<>(body, headers);
		try {
			restTemplate.postForEntity(reportsServiceUrl, entity, Void.class);
		} catch (RestClientException e) {
			log.warn("No se pudo replicar la alerta {} en ms-reportes: {}", alert.getId(), e.getMessage());
		}
	}
}
