package com.firewall.ms_usuarios.dto.request;

import jakarta.validation.constraints.NotBlank;

public class LoginRequestDTO {

    @NotBlank(message = "El RUT es obligatorio")
    private String rut;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
