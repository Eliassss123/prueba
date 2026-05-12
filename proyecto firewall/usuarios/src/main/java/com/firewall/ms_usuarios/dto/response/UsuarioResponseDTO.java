package com.firewall.ms_usuarios.dto.response;

public class UsuarioResponseDTO {

    private Long id;
    private String rut;
    private String email;
    private String nombre;

    public UsuarioResponseDTO() {
    }

    public UsuarioResponseDTO(Long id, String rut, String email, String nombre) {
        this.id = id;
        this.rut = rut;
        this.email = email;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
