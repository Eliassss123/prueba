package com.firewall.ms_usuarios.dto.response;

public class LoginResponseDTO {

    private String rut;
    private String nombre;
    private String mensaje;
    private boolean authenticated;

    public LoginResponseDTO() {
    }

    public LoginResponseDTO(String rut, String nombre, boolean authenticated, String mensaje) {
        this.rut = rut;
        this.nombre = nombre;
        this.authenticated = authenticated;
        this.mensaje = mensaje;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public boolean isAuthenticated() {
        return authenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
    }
}
