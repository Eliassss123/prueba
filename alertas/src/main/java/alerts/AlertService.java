package alerts;

import alerts.dto.request.CreateAlertRequest;
import alerts.dto.response.AlertResponse;
import alerts.integration.ReportsClient;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;
    private final ReportsClient reportsClient;

    public AlertService(AlertRepository alertRepository, ReportsClient reportsClient) {
        this.alertRepository = alertRepository;
        this.reportsClient = reportsClient;
    }

    public List<AlertResponse> getAllAlerts() {
        return alertRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public Optional<AlertResponse> getAlertById(Long id) {
        return alertRepository.findById(id).map(this::toResponse);
    }

    public AlertResponse createAlert(CreateAlertRequest request) {
        Alert alert = new Alert();
        alert.setType(request.getType());
        alert.setLocation(request.getLocation());
        alert.setSeverity(request.getSeverity());
        alert.setDescription(request.getDescription());

        Alert saved = alertRepository.save(alert);
        reportsClient.enviarReporteDesdeAlerta(saved);

        return toResponse(saved);
    }

    public AlertResponse updateAlert(Long id, CreateAlertRequest request) {
        Optional<Alert> optional = alertRepository.findById(id);
        if (optional.isPresent()) {
            Alert alert = optional.get();
            alert.setType(request.getType());
            alert.setLocation(request.getLocation());
            alert.setSeverity(request.getSeverity());
            alert.setDescription(request.getDescription());
            Alert saved = alertRepository.save(alert);
            return toResponse(saved);
        } else {
            throw new RuntimeException("Alert not found");
        }
    }

    public void deleteAlert(Long id) {
        alertRepository.deleteById(id);
    }

    private AlertResponse toResponse(Alert alert) {
        AlertResponse response = new AlertResponse();
        response.setId(alert.getId());
        response.setTimestamp(alert.getTimestamp());
        response.setType(alert.getType());
        response.setLocation(alert.getLocation());
        response.setSeverity(alert.getSeverity());
        response.setDescription(alert.getDescription());
        return response;
    }
}
