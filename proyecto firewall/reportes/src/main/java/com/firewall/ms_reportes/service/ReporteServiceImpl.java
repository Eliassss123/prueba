package com.firewall.ms_reportes.service;

import com.firewall.ms_reportes.dto.request.CreateReporteRequestDTO;
import com.firewall.ms_reportes.dto.response.ReporteResponseDTO;
import com.firewall.ms_reportes.dto.response.UsuarioDTO;
import com.firewall.ms_reportes.dto.response.UbicacionDTO;
import com.firewall.ms_reportes.dto.response.MultimediaDTO;
import com.firewall.ms_reportes.entity.Reporte;
import com.firewall.ms_reportes.entity.Usuario;
import com.firewall.ms_reportes.entity.UbicacionReporte;
import com.firewall.ms_reportes.entity.Multimedia;
import com.firewall.ms_reportes.repository.ReporteRepository;
import com.firewall.ms_reportes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReporteServiceImpl implements ReporteService {

    @Autowired
    private ReporteRepository reporteRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public ReporteResponseDTO crearReporte(CreateReporteRequestDTO request, String ipCliente) {
        // 1. Manejar el Usuario (RUT e IP)
        Usuario user = new Usuario();
        user.setRut(request.getUsuario().getRut());
        user.setDireccionIp(ipCliente);

        // Buscamos si ya existe el usuario por RUT, si no, lo guardamos
        Usuario usuarioGuardado = usuarioRepository.findByRut(user.getRut())
                .orElseGet(() -> usuarioRepository.save(user));

        // 2. Crear el Reporte
        Reporte nuevoReporte = new Reporte();
        nuevoReporte.setUsuario(usuarioGuardado);
        nuevoReporte.setDescripcion(request.getDescripcion());

        // 3. Manejar Ubicacion si existe
        if (request.getUbicacion() != null) {
            UbicacionReporte ubicacion = new UbicacionReporte();
            ubicacion.setLatitud(request.getUbicacion().getLatitud());
            ubicacion.setLongitud(request.getUbicacion().getLongitud());
            ubicacion.setDireccionReferencial(request.getUbicacion().getDireccionReferencial());
            ubicacion.setReporte(nuevoReporte);
            nuevoReporte.setUbicacion(ubicacion);
        }

        // 4. Manejar Multimedia si existe
        if (request.getMultimedia() != null && !request.getMultimedia().isEmpty()) {
            List<Multimedia> multimediaList = request.getMultimedia().stream()
                .map(dto -> {
                    Multimedia multimedia = new Multimedia();
                    multimedia.setUrlS3(dto.getUrlS3());
                    multimedia.setTipoArchivo(dto.getTipoArchivo());
                    multimedia.setReporte(nuevoReporte);
                    return multimedia;
                })
                .collect(Collectors.toList());
            nuevoReporte.setMultimedia(multimediaList);
        }

        // 5. Guardar el Reporte
        Reporte reporteGuardado = reporteRepository.save(nuevoReporte);

        // 6. Mapear a Response DTO
        return mapToResponseDTO(reporteGuardado);
    }

    @Override
    public List<ReporteResponseDTO> listarReportes() {
        List<Reporte> reportes = reporteRepository.findAll();
        return reportes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public ReporteResponseDTO obtenerReporte(Long id) {
        Optional<Reporte> reporteOpt = reporteRepository.findById(id);
        if (reporteOpt.isPresent()) {
            return mapToResponseDTO(reporteOpt.get());
        } else {
            throw new RuntimeException("Reporte no encontrado con id: " + id);
        }
    }

    @Override
    public ReporteResponseDTO actualizarReporte(Long id, CreateReporteRequestDTO request, String ipCliente) {
        Optional<Reporte> reporteOpt = reporteRepository.findById(id);
        if (reporteOpt.isPresent()) {
            Reporte reporte = reporteOpt.get();

            // Actualizar descripción si proporcionada
            if (request.getDescripcion() != null) {
                reporte.setDescripcion(request.getDescripcion());
            }

            // Actualizar usuario si proporcionado
            if (request.getUsuario() != null && request.getUsuario().getRut() != null) {
                Usuario user = new Usuario();
                user.setRut(request.getUsuario().getRut());
                user.setDireccionIp(ipCliente);
                Usuario usuarioGuardado = usuarioRepository.findByRut(user.getRut())
                        .orElseGet(() -> usuarioRepository.save(user));
                reporte.setUsuario(usuarioGuardado);
            }

            // Actualizar ubicación si proporcionada
            if (request.getUbicacion() != null) {
                UbicacionReporte ubicacion = reporte.getUbicacion() != null ? reporte.getUbicacion() : new UbicacionReporte();
                ubicacion.setLatitud(request.getUbicacion().getLatitud());
                ubicacion.setLongitud(request.getUbicacion().getLongitud());
                ubicacion.setDireccionReferencial(request.getUbicacion().getDireccionReferencial());
                ubicacion.setReporte(reporte);
                reporte.setUbicacion(ubicacion);
            }

            // Actualizar multimedia si proporcionada
            if (request.getMultimedia() != null) {
                List<Multimedia> multimediaList = request.getMultimedia().stream()
                    .map(dto -> {
                        Multimedia multimedia = new Multimedia();
                        multimedia.setUrlS3(dto.getUrlS3());
                        multimedia.setTipoArchivo(dto.getTipoArchivo());
                        multimedia.setReporte(reporte);
                        return multimedia;
                    })
                    .collect(Collectors.toList());
                reporte.setMultimedia(multimediaList);
            }

            Reporte reporteGuardado = reporteRepository.save(reporte);
            return mapToResponseDTO(reporteGuardado);
        } else {
            throw new RuntimeException("Reporte no encontrado con id: " + id);
        }
    }

    @Override
    public void eliminarReporte(Long id) {
        if (!reporteRepository.existsById(id)) {
            throw new RuntimeException("Reporte no encontrado con id: " + id);
        }
        reporteRepository.deleteById(id);
    }

    @Override
    public List<ReporteResponseDTO> obtenerReportesPorUsuario(String rut) {
        List<Reporte> reportes = reporteRepository.findByUsuario_Rut(rut);
        return reportes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    @Override
    public List<ReporteResponseDTO> obtenerReportesPorEstado(String estado) {
        List<Reporte> reportes = reporteRepository.findByEstado(estado);
        return reportes.stream().map(this::mapToResponseDTO).collect(Collectors.toList());
    }

    private ReporteResponseDTO mapToResponseDTO(Reporte reporte) {
        ReporteResponseDTO dto = new ReporteResponseDTO();
        dto.setId(reporte.getId());
        dto.setDescripcion(reporte.getDescripcion());
        dto.setFechaHora(reporte.getFechaHora());
        dto.setEstado(reporte.getEstado());

        // Usuario
        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setId(reporte.getUsuario().getId());
        usuarioDTO.setRut(reporte.getUsuario().getRut());
        usuarioDTO.setDireccionIp(reporte.getUsuario().getDireccionIp());
        dto.setUsuario(usuarioDTO);

        // Ubicacion
        if (reporte.getUbicacion() != null) {
            UbicacionDTO ubicacionDTO = new UbicacionDTO();
            ubicacionDTO.setLatitud(reporte.getUbicacion().getLatitud());
            ubicacionDTO.setLongitud(reporte.getUbicacion().getLongitud());
            ubicacionDTO.setDireccionReferencial(reporte.getUbicacion().getDireccionReferencial());
            dto.setUbicacion(ubicacionDTO);
        }

        // Multimedia
        if (reporte.getMultimedia() != null) {
            List<MultimediaDTO> multimediaDTOs = reporte.getMultimedia().stream()
                .map(m -> {
                    MultimediaDTO multimediaDTO = new MultimediaDTO();
                    multimediaDTO.setId(m.getId());
                    multimediaDTO.setUrlS3(m.getUrlS3());
                    multimediaDTO.setTipoArchivo(m.getTipoArchivo());
                    return multimediaDTO;
                })
                .collect(Collectors.toList());
            dto.setMultimedia(multimediaDTOs);
        }

        return dto;
    }
}