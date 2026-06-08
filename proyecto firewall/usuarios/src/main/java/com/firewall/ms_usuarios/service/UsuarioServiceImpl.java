package com.firewall.ms_usuarios.service;

import com.firewall.ms_usuarios.dto.request.ChangePasswordRequestDTO;
import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;
import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private static final String TEMP_PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789@#$%";
    private static final int TEMP_PASSWORD_LENGTH = 12;

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Override
    public Usuario register(RegisterRequestDTO request) {
        String rut = request.getRut().trim();
        if (usuarioRepository.findByRut(rut).isPresent()) {
            throw new IllegalArgumentException("Ya existe un usuario registrado con ese RUT");
        }

        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setEmail(request.getEmail().toLowerCase().trim());
        usuario.setNombre(request.getNombre());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario login(LoginRequestDTO request) {
        return usuarioRepository.findByRut(request.getRut().trim())
                .filter(usuario -> passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
    }

    @Override
    public Usuario findByRut(String rut) {
        return usuarioRepository.findByRut(rut.trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    @Override
    public String recoverPasswordByEmail(String email) {
        String normalized = email == null ? "" : email.toLowerCase().trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("El correo electrónico es obligatorio");
        }
        return usuarioRepository.findByEmailIgnoreCase(normalized).map(usuario -> {
            String temporary = generateTemporaryPassword();
            String previousPasswordHash = usuario.getPasswordHash();
            usuario.setPasswordHash(passwordEncoder.encode(temporary));
            usuarioRepository.save(usuario);
            if (!emailService.isConfigured()) {
                return "Modo demo: correo no configurado. Contrasena provisional: " + temporary;
            }
            try {
                emailService.sendTemporaryPassword(usuario.getEmail(), usuario.getNombre(), usuario.getRut(), temporary);
            } catch (MessagingException ex) {
                usuario.setPasswordHash(previousPasswordHash);
                usuarioRepository.save(usuario);
                throw new IllegalStateException("No se pudo enviar el correo de recuperación", ex);
            }
            return "Si el correo esta registrado, recibira en breve una contrasena provisional.";
        }).orElse("Si el correo esta registrado, recibira en breve una contrasena provisional.");
    }

    @Override
    public void changePassword(ChangePasswordRequestDTO request) {
        if (!request.getNewPassword().equals(request.getConfirmNewPassword())) {
            throw new IllegalArgumentException("La nueva contraseña y su confirmación no coinciden");
        }
        Usuario usuario = usuarioRepository.findByRut(request.getRut().trim())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), usuario.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        usuario.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        usuarioRepository.save(usuario);
    }

    private String generateTemporaryPassword() {
        StringBuilder sb = new StringBuilder(TEMP_PASSWORD_LENGTH);
        for (int i = 0; i < TEMP_PASSWORD_LENGTH; i++) {
            sb.append(TEMP_PASSWORD_ALPHABET.charAt(secureRandom.nextInt(TEMP_PASSWORD_ALPHABET.length())));
        }
        return sb.toString();
    }
}
