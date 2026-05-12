package com.firewall.ms_reportes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "Ubicacion_Reporte")
@Data
public class UbicacionReporte {

    @Id
    @Column(name = "reporte_id")
    private Long id;

    @OneToOne
    @MapsId
    @JoinColumn(name = "reporte_id")
    @JsonIgnore // Evita bucles infinitos al devolver el JSON
    private Reporte reporte;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitud;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitud;

    @Column(name = "direccion_referencial")
    private String direccionReferencial;
}