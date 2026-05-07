package com.firewall.ms_reportes.repository;

import com.firewall.ms_reportes.entity.Reporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteRepository extends JpaRepository<Reporte, Long> {
    List<Reporte> findByUsuario_Rut(String rut);
    List<Reporte> findByEstado(String estado);
}