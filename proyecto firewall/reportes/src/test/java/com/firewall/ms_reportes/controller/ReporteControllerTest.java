package com.firewall.ms_reportes.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firewall.ms_reportes.dto.request.CreateReporteRequestDTO;
import com.firewall.ms_reportes.dto.request.MultimediaRequestDTO;
import com.firewall.ms_reportes.dto.request.UsuarioRequestDTO;
import com.firewall.ms_reportes.dto.request.UbicacionRequestDTO;
import com.firewall.ms_reportes.dto.response.ReporteResponseDTO;
import com.firewall.ms_reportes.service.ReporteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReporteControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReporteService reporteService;

    @InjectMocks
    private ReporteController reporteController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reporteController).build();
    }

    @Test
    void crearReporte_debeRetornarReporte() throws Exception {
        CreateReporteRequestDTO request = new CreateReporteRequestDTO();
        UsuarioRequestDTO usuario = new UsuarioRequestDTO();
        usuario.setRut("12.345.678-9");
        request.setUsuario(usuario);
        request.setDescripcion("Incendio en edificio");
        UbicacionRequestDTO ubicacion = new UbicacionRequestDTO();
        ubicacion.setLatitud(new BigDecimal("-33.4489"));
        ubicacion.setLongitud(new BigDecimal("-70.6693"));
        ubicacion.setDireccionReferencial("Av. Siempre Viva 123");
        request.setUbicacion(ubicacion);
        MultimediaRequestDTO multimedia = new MultimediaRequestDTO();
        multimedia.setUrlS3("https://cdn.ejemplo.cl/foto.jpg");
        multimedia.setTipoArchivo("image/jpeg");
        request.setMultimedia(List.of(multimedia));

        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(1L);
        response.setDescripcion("Incendio en edificio");
        response.setEstado("Enviado");
        response.setFechaHora(LocalDateTime.now());

        when(reporteService.crearReporte(any(CreateReporteRequestDTO.class), any(String.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/reportes/enviar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.descripcion").value("Incendio en edificio"))
            .andExpect(jsonPath("$.estado").value("Enviado"));
    }

    @Test
    void listarReportes_debeRetornarListaVacia() throws Exception {
        when(reporteService.listarReportes()).thenReturn(List.of());

        mockMvc.perform(get("/api/reportes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0]").doesNotExist());
    }

    @Test
    void obtenerReportePorId_debeRetornarReporte() throws Exception {
        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(2L);
        response.setDescripcion("Reporte existente");
        response.setEstado("Enviado");
        response.setFechaHora(LocalDateTime.now());

        when(reporteService.obtenerReporte(2L)).thenReturn(response);

        mockMvc.perform(get("/api/reportes/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.descripcion").value("Reporte existente"));
    }

    @Test
    void eliminarReporte_debeRetornar204() throws Exception {
        mockMvc.perform(delete("/api/reportes/2"))
            .andExpect(status().isNoContent());

        verify(reporteService).eliminarReporte(2L);
    }
}
