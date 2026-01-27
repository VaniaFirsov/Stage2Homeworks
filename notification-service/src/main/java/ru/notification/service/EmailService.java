package ru.notification.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendSimpleMessage(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);

            mailSender.send(message);
            log.info("Email отправлен на адрес: {}", to);
        } catch (Exception e) {
            log.error("Ошибка при отправке email на адрес {}: {}", to, e.getMessage());
            throw new RuntimeException("Ошибка при отправке email", e);
        }
    }

    public void sendUserCreationEmail(String email, String name) {
        String subject = "Добро пожаловать!";
        String text = String.format("""
            Здравствуйте, %s!
            
            Ваш аккаунт на сайте ваш сайт был успешно создан.
            
            С уважением,
            Команда сайта
            """, name);

        sendSimpleMessage(email, subject, text);
    }

    public void sendUserDeletionEmail(String email, String name) {
        String subject = "Ваш аккаунт удален";
        String text = String.format("""
            Здравствуйте, %s!
            
            Ваш аккаунт был удалён.
            
            С уважением,
            Команда сайта
            """, name);

        sendSimpleMessage(email, subject, text);
    }
}
