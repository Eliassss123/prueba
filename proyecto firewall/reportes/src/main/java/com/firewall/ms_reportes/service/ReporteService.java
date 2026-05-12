package com.firewall.ms_reportes.service;

import com.firewall.ms_reportes.dto.request.CreateReporteRequestDTO;
import com.firewall.ms_reportes.dto.response.ReporteResponseDTO;

import java.util.List;

public interface ReporteService {
    ReporteResponseDTO crearReporte(CreateReporteRequestDTO request, String ipCliente);
    List<ReporteResponseDTO> listarReportes();
    ReporteResponseDTO obtenerReporte(Long id);
    ReporteResponseDTO actualizarReporte(Long id, CreateReporteRequestDTO request, String ipCliente);
    void eliminarReporte(Long id);
    List<ReporteResponseDTO> obtenerReportesPorUsuario(String rut);
    List<ReporteResponseDTO> obtenerReportesPorEstado(String estado);
}