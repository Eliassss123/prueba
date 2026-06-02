package com.firewall.ms_usuarios.controller;

import com.firewall.ms_usuarios.dto.request.ChangePasswordRequestDTO;
import com.firewall.ms_usuarios.dto.request.LoginRequestDTO;
import com.firewall.ms_usuarios.dto.request.RegisterRequestDTO;
import com.firewall.ms_usuarios.dto.response.LoginResponseDTO;
import com.firewall.ms_usuarios.dto.response.UsuarioResponseDTO;
import com.firewall.ms_usuarios.entity.Usuario;
import com.firewall.ms_usuarios.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = {"*", "X-Forwarded-Host", "x-forwarded-host"})
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        Usuario usuario = usuarioService.register(request);
        UsuarioResponseDTO response = new UsuarioResponseDTO(usuario.getId(), usuario.getRut(), usuario.getEmail(), usuario.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        usuarioService.recoverPasswordByEmail(email);
        return ResponseEntity.ok(Collections.singletonMap(
                "message",
                "Si el correo está registrado, recibirá en breve una contraseña provisional."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        Usuario usuario = usuarioService.login(request);
        LoginResponseDTO response = new LoginResponseDTO(usuario.getRut(), usuario.getNombre(), true, "Login exitoso");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@Valid @RequestBody ChangePasswordRequestDTO request) {
        usuarioService.changePassword(request);
        return ResponseEntity.ok(Collections.singletonMap("message", "Contraseña actualizada correctamente"));
    }

    @GetMapping("/{rut}")
    public ResponseEntity<UsuarioResponseDTO> getUsuario(@PathVariable String rut) {
        Usuario usuario = usuarioService.findByRut(rut);
        UsuarioResponseDTO response = new UsuarioResponseDTO(usuario.getId(), usuario.getRut(), usuario.getEmail(), usuario.getNombre());
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleJsonParseError(HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", "El cuerpo de la solicitud no es un JSON válido."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Collections.singletonMap("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Collections.singletonMap("message", ex.getMessage()));
    }
}
