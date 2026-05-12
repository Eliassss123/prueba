package com.firewall.geolocalizacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
	name = "CoordenadaRequest",
	description = "Coordenadas WGS84 y descripción del punto de monitoreo (creación o actualización de registro)."
)
public class CoordenadaRequest {

	@NotNull(message = "La latitud es obligatoria")
	@Schema(description = "Latitud decimal", example = "-33.4489", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double latitud;

	@NotNull(message = "La longitud es obligatoria")
	@Schema(description = "Longitud decimal", example = "-70.6693", requiredMode = Schema.RequiredMode.REQUIRED)
	private Double longitud;

	@NotBlank(message = "La descripción es obligatoria")
	@Schema(description = "Descripción del punto o contexto operativo", example = "Puesto de mando improvisado", requiredMode = Schema.RequiredMode.REQUIRED)
	private String descripcion;

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}