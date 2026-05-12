package com.firewall.ms_reportes.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Multimedia")
@Data
public class Multimedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "reporte_id")
    @JsonIgnore
    private Reporte reporte;

    @Column(name = "url_s3")
    private String urlS3;

    @Column(name = "tipo_archivo", length = 20)
    private String tipoArchivo; // 'foto' o 'video'
}