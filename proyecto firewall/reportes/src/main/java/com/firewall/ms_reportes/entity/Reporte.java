package com.firewall.ms_reportes.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Reporte")
@Data
public class Reporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne // Muchos reportes pueden ser de un mismo RUT/IP en una emergencia
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    private String descripcion;

    @Column(name = "fecha_hora")
    private LocalDateTime fechaHora = LocalDateTime.now();

    private String estado = "Enviado";

    // ... (código que ya tenías: id, usuario, descripcion, etc.) ...

    // Relación 1 a 1 con Ubicación (Un reporte tiene una ubicación)
    @OneToOne(mappedBy = "reporte", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private UbicacionReporte ubicacion;

    // Relación 1 a Muchos con Multimedia (Un reporte puede tener varias fotos)
    @OneToMany(mappedBy = "reporte", cascade = CascadeType.ALL)
    private java.util.List<Multimedia> multimedia;
}