package com.firewall.ms_usuarios.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;
import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController).build();
    }

    @Test
    void registroDeUsuario_debeRetornar201_yDTO() throws Exception {
        RegisterRequestDTO request = new RegisterRequestDTO();
        request.setRut("12.345.678-9");
        request.setEmail("juan@example.com");
        request.setPassword("password123");
        request.setNombre("Juan Perez");

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setRut("12.345.678-9");
        usuario.setEmail("juan@example.com");
        usuario.setNombre("Juan Perez");

        when(usuarioService.register(any(RegisterRequestDTO.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.rut").value("12.345.678-9"))
            .andExpect(jsonPath("$.email").value("juan@example.com"))
            .andExpect(jsonPath("$.nombre").value("Juan Perez"));
    }

    @Test
    void loginDeUsuario_debeRetornar200_yDTO() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setRut("12.345.678-9");
        request.setPassword("password123");

        Usuario usuario = new Usuario();
        usuario.setRut("12.345.678-9");
        usuario.setNombre("Juan Perez");

        when(usuarioService.login(any(LoginRequestDTO.class))).thenReturn(usuario);

        mockMvc.perform(post("/api/usuarios/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.rut").value("12.345.678-9"))
            .andExpect(jsonPath("$.nombre").value("Juan Perez"))
            .andExpect(jsonPath("$.authenticated").value(true))
            .andExpect(jsonPath("$.mensaje").value("Login exitoso"));
    }

    @Test
    void obtenerUsuarioPorRut_debeRetornar200_yDTO() throws Exception {
        Usuario usuario = new Usuario();
        usuario.setId(2L);
        usuario.setRut("98.765.432-1");
        usuario.setEmail("ana@example.com");
        usuario.setNombre("Ana Gómez");

        when(usuarioService.findByRut("98.765.432-1")).thenReturn(usuario);

        mockMvc.perform(get("/api/usuarios/98.765.432-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(2))
            .andExpect(jsonPath("$.rut").value("98.765.432-1"))
            .andExpect(jsonPath("$.email").value("ana@example.com"))
            .andExpect(jsonPath("$.nombre").value("Ana Gómez"));
    }

    @Test
    void registerInvalidRequest_debeRetornar400_conMensajeDeValidacion() throws Exception {
        Map<String, Object> invalidRequest = Map.of("rut", "12.345.678-9", "nombre", "Test");

        mockMvc.perform(post("/api/usuarios/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").exists());
    }
}
