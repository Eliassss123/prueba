package com.firewall.ms_reportes.repository;

import com.firewall.ms_reportes.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Esto nos servirá para no duplicar el "registro" si el mismo RUT manda otro reporte
    Optional<Usuario> findByRut(String rut);
}