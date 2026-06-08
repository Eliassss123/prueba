package com.firewall.ms_usuarios.service;

import com.firewall.ms_usuarios.dto.request.ChangePasswordRequestDTO;
import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;
import com.firewall.ms_usuarios.entity.Usuario;

public interface UsuarioService {
    Usuario register(RegisterRequestDTO request);
    Usuario login(LoginRequestDTO request);
    Usuario findByRut(String rut);
    String recoverPasswordByEmail(String email);
    void changePassword(ChangePasswordRequestDTO request);
}
