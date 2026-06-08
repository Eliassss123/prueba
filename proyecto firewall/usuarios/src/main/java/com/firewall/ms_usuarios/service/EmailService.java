package com.firewall.ms_usuarios.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String fromAddress;
    private final String password;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username:}") String fromAddress,
            @Value("${spring.mail.password:}") String password) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
        this.password = password;
    }

    public void validateConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "El correo de recuperacion no esta configurado. Defina MAIL_USERNAME y MAIL_APP_PASSWORD.");
        }
    }

    public boolean isConfigured() {
        return fromAddress != null && !fromAddress.isBlank() && password != null && !password.isBlank();
    }

    public void sendTemporaryPassword(String toEmail, String nombre, String rut, String temporaryPassword)
            throws MessagingException {
        validateConfigured();
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setFrom(fromAddress);
        helper.setTo(toEmail);
        helper.setSubject("Proyecto Firewall — Recuperación de contraseña");
        helper.setText(buildHtml(nombre, rut, temporaryPassword), true);
        mailSender.send(message);
    }

    private String buildHtml(String nombre, String rut, String temporaryPassword) {
        String displayName = nombre != null && !nombre.isBlank() ? nombre : "Estimado/a usuario/a";
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head><meta charset="UTF-8"></head>
            <body style="font-family:Segoe UI,Arial,sans-serif;background:#f8fafc;margin:0;padding:24px;">
              <table width="100%%" cellpadding="0" cellspacing="0" style="max-width:560px;margin:0 auto;background:#ffffff;border-radius:12px;box-shadow:0 4px 12px rgba(15,23,42,0.08);">
                <tr>
                  <td style="background:#ea580c;padding:20px 24px;border-radius:12px 12px 0 0;">
                    <h1 style="margin:0;color:#ffffff;font-size:20px;">Proyecto Firewall</h1>
                    <p style="margin:6px 0 0;color:#ffedd5;font-size:13px;">Red de respuesta · Chile</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding:24px;color:#334155;font-size:15px;line-height:1.6;">
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Recibimos una solicitud de recuperación de contraseña asociada al RUT <strong>%s</strong>.</p>
                    <p>Su clave provisional de acceso es:</p>
                    <p style="text-align:center;margin:20px 0;">
                      <span style="display:inline-block;padding:12px 20px;background:#f1f5f9;border:1px solid #e2e8f0;border-radius:8px;font-size:18px;font-weight:700;letter-spacing:1px;color:#0f172a;">%s</span>
                    </p>
                    <p>Por seguridad, le recomendamos iniciar sesión y cambiar esta contraseña desde el panel <em>Mi Perfil</em>.</p>
                    <p style="font-size:13px;color:#64748b;margin-top:24px;">Si usted no solicitó este cambio, contacte al administrador del sistema de inmediato.</p>
                  </td>
                </tr>
                <tr>
                  <td style="padding:16px 24px;background:#f8fafc;border-radius:0 0 12px 12px;font-size:12px;color:#94a3b8;">
                    Mensaje automático — no responda a este correo.
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(displayName, rut, temporaryPassword);
    }
}
