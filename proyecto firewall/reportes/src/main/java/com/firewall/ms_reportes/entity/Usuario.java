package com.firewall.ms_reportes.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Usuario")
@Data // Esto crea automáticamente Getters y Setters gracias a Lombok
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 20)
    private String rut;

    @Column(name = "direccion_ip", nullable = false, length = 45)
    private String direccionIp;
}