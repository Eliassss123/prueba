package com.firewall.ms_usuarios.service;

import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;
import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
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
}
