package com.firewall.ms_usuarios.service;

import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class EmailServiceTest {

    @Test
    void validateConfigured_debeFallarSiNoHayCredencialesSmtp() {
        EmailService emailService = new EmailService(mock(JavaMailSender.class), "", "");

        assertThrows(IllegalStateException.class, emailService::validateConfigured);
    }
}
