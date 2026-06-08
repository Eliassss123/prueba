package com.firewall.ms_usuarios.service;

import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private EmailService emailService;

    private UsuarioServiceImpl usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, emailService);
    }

    @Test
    void recoverPasswordByEmail_debeActualizarPasswordYEnviarCorreo() throws Exception {
        Usuario usuario = usuarioRegistrado();
        when(usuarioRepository.findByEmailIgnoreCase("juan@example.com")).thenReturn(Optional.of(usuario));
        when(emailService.isConfigured()).thenReturn(true);

        usuarioService.recoverPasswordByEmail(" Juan@Example.com ");

        ArgumentCaptor<String> temporaryPassword = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendTemporaryPassword(
                eq("juan@example.com"),
                eq("Juan Perez"),
                eq("12.345.678-9"),
                temporaryPassword.capture());
        verify(usuarioRepository).save(usuario);
        assertEquals(12, temporaryPassword.getValue().length());
        assertTrue(new BCryptPasswordEncoder().matches(temporaryPassword.getValue(), usuario.getPasswordHash()));
    }

    @Test
    void recoverPasswordByEmail_debeRestaurarPasswordAnteriorSiFallaElCorreo() throws Exception {
        Usuario usuario = usuarioRegistrado();
        String previousPasswordHash = usuario.getPasswordHash();
        when(usuarioRepository.findByEmailIgnoreCase("juan@example.com")).thenReturn(Optional.of(usuario));
        when(emailService.isConfigured()).thenReturn(true);
        doThrow(new MessagingException("smtp error"))
                .when(emailService)
                .sendTemporaryPassword(eq("juan@example.com"), eq("Juan Perez"), eq("12.345.678-9"), anyString());

        assertThrows(IllegalStateException.class, () -> usuarioService.recoverPasswordByEmail("juan@example.com"));

        verify(usuarioRepository, times(2)).save(usuario);
        assertEquals(previousPasswordHash, usuario.getPasswordHash());
    }

    @Test
    void recoverPasswordByEmail_debeRetornarPasswordTemporalEnModoDemoSiNoHayCorreoConfigurado() {
        Usuario usuario = usuarioRegistrado();
        when(usuarioRepository.findByEmailIgnoreCase("juan@example.com")).thenReturn(Optional.of(usuario));
        when(emailService.isConfigured()).thenReturn(false);

        String message = usuarioService.recoverPasswordByEmail("juan@example.com");

        assertTrue(message.startsWith("Modo demo: correo no configurado. Contrasena provisional: "));
        String temporary = message.substring(message.lastIndexOf(": ") + 2);
        assertEquals(12, temporary.length());
        assertTrue(new BCryptPasswordEncoder().matches(temporary, usuario.getPasswordHash()));
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void recoverPasswordByEmail_noDebeEnviarCorreoSiElEmailNoExiste() {
        when(usuarioRepository.findByEmailIgnoreCase("nadie@example.com")).thenReturn(Optional.empty());

        usuarioService.recoverPasswordByEmail("nadie@example.com");

        verifyNoInteractions(emailService);
    }

    private Usuario usuarioRegistrado() {
        Usuario usuario = new Usuario();
        usuario.setRut("12.345.678-9");
        usuario.setEmail("juan@example.com");
        usuario.setNombre("Juan Perez");
        usuario.setPasswordHash(new BCryptPasswordEncoder().encode("password123"));
        return usuario;
    }
}
