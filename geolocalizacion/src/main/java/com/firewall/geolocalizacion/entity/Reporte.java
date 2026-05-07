package com.firewall.geolocalizacion.entity;



import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reportes")
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String googleMapsUrl;

    @OneToOne
    @JoinColumn(name = "coordenada_id")
    private Coordenada coordenada;

    private LocalDateTime fechaGeneracion;

    public Reporte() {}

    public Reporte(String googleMapsUrl, Coordenada coordenada, LocalDateTime fechaGeneracion) {
        this.googleMapsUrl = googleMapsUrl;
        this.coordenada = coordenada;
        this.fechaGeneracion = fechaGeneracion;
    }

    public Long getId() {
        return id;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    public Coordenada getCoordenada() {
        return coordenada;
    }

    public void setCoordenada(Coordenada coordenada) {
        this.coordenada = coordenada;
    }

    public LocalDateTime getFechaGeneracion() {
        return fechaGeneracion;
    }

    public void setFechaGeneracion(LocalDateTime fechaGeneracion) {
        this.fechaGeneracion = fechaGeneracion;
    }
}