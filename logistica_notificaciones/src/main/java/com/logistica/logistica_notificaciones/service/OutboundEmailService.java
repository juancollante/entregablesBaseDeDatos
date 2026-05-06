package com.logistica.logistica_notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;

@Service
public class OutboundEmailService {

    private static final Logger log = LoggerFactory.getLogger(OutboundEmailService.class);

    private final JavaMailSender mailSender;
    private final String from;

    public OutboundEmailService(
            JavaMailSender mailSender,
            @Value("${app.mail.from:no-reply@logistica.local}") String from,
            @Value("${spring.mail.host:}") String mailHost
    ) {
        this.mailSender = mailSender;
        this.from = from;
        // mailHost se recibe para permitir que Spring lo inyecte desde env; el envío valida el sender real.
    }

    public void trySendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            return;
        }
        // Si el starter-mail está en el classpath pero no hay configuración, Spring igual crea un JavaMailSender.
        // Validamos host aquí para evitar fallos en runtime cuando el envío es opcional.
        if (mailSender instanceof JavaMailSenderImpl impl) {
            String configuredHost = impl.getHost();
            if (configuredHost == null || configuredHost.isBlank()) {
                log.info("Email omitido (JavaMailSender sin host). to={} subject={}", to, subject);
                return;
            }
        }

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(to);
        msg.setSubject(subject == null ? "Notificación" : subject);
        msg.setText(body == null ? "" : body);
        mailSender.send(msg);
    }
}

