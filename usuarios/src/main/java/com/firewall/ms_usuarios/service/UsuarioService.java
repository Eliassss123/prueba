package com.firewall.ms_usuarios.service;

import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;

public interface UsuarioService {
    Usuario register(RegisterRequestDTO request);
    Usuario login(LoginRequestDTO request);
    Usuario findByRut(String rut);
}
