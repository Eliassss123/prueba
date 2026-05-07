package com.firewall.geolocalizacion.repository;

import com.firewall.geolocalizacion.entity.Coordenada;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoordenadaRepository extends JpaRepository<Coordenada, Long> {
}