package com.firewall.geolocalizacion.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firewall.geolocalizacion.dto.CoordenadaRequest;
import com.firewall.geolocalizacion.dto.response.ReporteResponseDTO;
import com.firewall.geolocalizacion.service.MonitoreoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MonitoreoControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MonitoreoService service;

    @InjectMocks
    private MonitoreoController monitoreoController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(monitoreoController).build();
    }

    @Test
    void crearRegistro_debeRetornarReporteConUrl() throws Exception {
        CoordenadaRequest request = new CoordenadaRequest();
        request.setLatitud(-33.4489);
        request.setLongitud(-70.6693);
        request.setDescripcion("Punto de monitoreo");

        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(1L);
        response.setGoogleMapsUrl("https://www.google.com/maps?q=-33.4489,-70.6693");
        response.setLatitud(-33.4489);
        response.setLongitud(-70.6693);
        response.setFechaGeneracion(LocalDateTime.now());

        when(service.procesarDatos(any(CoordenadaRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/monitoreo")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.googleMapsUrl").value("https://www.google.com/maps?q=-33.4489,-70.6693"));
    }

    @Test
    void obtenerReportePorId_debeRetornarReporte() throws Exception {
        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(2L);
        response.setGoogleMapsUrl("https://www.google.com/maps?q=-33.4490,-70.6700");
        response.setLatitud(-33.4490);
        response.setLongitud(-70.6700);
        response.setFechaGeneracion(LocalDateTime.now());

        when(service.obtenerReporte(2L)).thenReturn(response);

        mockMvc.perform(get("/api/monitoreo/2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.googleMapsUrl").value("https://www.google.com/maps?q=-33.4490,-70.6700"));
    }

    @Test
    void listarReportes_debeRetornarLista() throws Exception {
        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(3L);
        response.setGoogleMapsUrl("https://www.google.com/maps?q=-33.4489,-70.6693");
        response.setLatitud(-33.4489);
        response.setLongitud(-70.6693);
        response.setFechaGeneracion(LocalDateTime.now());

        when(service.obtenerReportes()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/monitoreo"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(3))
            .andExpect(jsonPath("$[0].googleMapsUrl").value("https://www.google.com/maps?q=-33.4489,-70.6693"));
    }

    @Test
    void actualizarRegistro_debeRetornarReporteActualizado() throws Exception {
        CoordenadaRequest request = new CoordenadaRequest();
        request.setLatitud(-33.4470);
        request.setLongitud(-70.6670);
        request.setDescripcion("Actualizado");

        ReporteResponseDTO response = new ReporteResponseDTO();
        response.setId(5L);
        response.setGoogleMapsUrl("https://www.google.com/maps?q=-33.4470,-70.6670");
        response.setLatitud(-33.4470);
        response.setLongitud(-70.6670);
        response.setFechaGeneracion(LocalDateTime.now());

        when(service.actualizarReporte(anyLong(), any(CoordenadaRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/monitoreo/5")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(5))
            .andExpect(jsonPath("$.googleMapsUrl").value("https://www.google.com/maps?q=-33.4470,-70.6670"));
    }

    @Test
    void eliminarRegistro_debeRetornar200() throws Exception {
        mockMvc.perform(delete("/api/monitoreo/5"))
            .andExpect(status().isOk());

        verify(service).eliminarReporte(5L);
    }
}
