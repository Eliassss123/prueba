package com.firewall.geolocalizacion.service;

import java.util.List;
import com.firewall.geolocalizacion.dto.CoordenadaRequest;
import com.firewall.geolocalizacion.dto.response.ReporteResponseDTO;
import com.firewall.geolocalizacion.entity.Coordenada;
import com.firewall.geolocalizacion.entity.Reporte;
import com.firewall.geolocalizacion.repository.CoordenadaRepository;
import com.firewall.geolocalizacion.repository.ReporteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class MonitoreoService {

    private final CoordenadaRepository coordenadaRepository;
    private final ReporteRepository reporteRepository;

    public MonitoreoService(CoordenadaRepository coordenadaRepository,
                            ReporteRepository reporteRepository) {
        this.coordenadaRepository = coordenadaRepository;
        this.reporteRepository = reporteRepository;
    }

    public List<ReporteResponseDTO> obtenerReportes() {
        return reporteRepository.findAll().stream()
            .map(this::toResponse)
            .toList();
    }

    public ReporteResponseDTO procesarDatos(CoordenadaRequest request) {
        Double lat = request.getLatitud();
        Double lon = request.getLongitud();
        String descripcion = request.getDescripcion();

        // Crear coordenada
        Coordenada coordenada = new Coordenada();
        coordenada.setLatitud(lat);
        coordenada.setLongitud(lon);
        coordenada.setDescripcion(descripcion);
        coordenada = coordenadaRepository.save(coordenada);

        // Crear reporte
        Reporte reporte = new Reporte();
        reporte.setGoogleMapsUrl("https://www.google.com/maps?q=" + lat + "," + lon);
        reporte.setCoordenada(coordenada);
        reporte.setFechaGeneracion(LocalDateTime.now());
        reporte = reporteRepository.save(reporte);

        return toResponse(reporte);
    }

    public ReporteResponseDTO obtenerReporte(Long id) {
        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));
        return toResponse(reporte);
    }

    public ReporteResponseDTO actualizarReporte(Long id, CoordenadaRequest request) {
        Reporte reporte = reporteRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reporte no encontrado"));

        Coordenada coordenada = reporte.getCoordenada();
        if (coordenada == null) {
            coordenada = new Coordenada();
        }
        coordenada.setLatitud(request.getLatitud());
        coordenada.setLongitud(request.getLongitud());
        coordenada.setDescripcion(request.getDescripcion());
        coordenada = coordenadaRepository.save(coordenada);

        reporte.setGoogleMapsUrl("https://www.google.com/maps?q=" + request.getLatitud() + "," + request.getLongitud());
        reporte = reporteRepository.save(reporte);

        return toResponse(reporte);
    }

    public void eliminarReporte(Long id) {
        reporteRepository.deleteById(id);
    }

    private ReporteResponseDTO toResponse(Reporte reporte) {
        ReporteResponseDTO dto = new ReporteResponseDTO();
        dto.setId(reporte.getId());
        dto.setGoogleMapsUrl(reporte.getGoogleMapsUrl());
        if (reporte.getCoordenada() != null) {
            dto.setLatitud(reporte.getCoordenada().getLatitud());
            dto.setLongitud(reporte.getCoordenada().getLongitud());
        }
        dto.setFechaGeneracion(reporte.getFechaGeneracion());
        return dto;
    }
}