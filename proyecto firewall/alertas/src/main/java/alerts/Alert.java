package alerts;

/**
 * Entidad que representa una alerta del sistema de monitoreo.
 */
public class Alert {

	private Long id;
	private String type;
	private String severity;
	private String location;
	private String description;

	public Alert() {
	}

	public Alert(Long id, String type, String severity, String location, String description) {
		this.id = id;
		this.type = type;
		this.severity = severity;
		this.location = location;
		this.description = description;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSeverity() {
		return severity;
	}

	public void setSeverity(String severity) {
		this.severity = severity;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
