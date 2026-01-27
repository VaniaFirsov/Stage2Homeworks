package ru.notification.controller;

import ru.notification.dto.EmailRequestDTO;
import ru.notification.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/email")
    public ResponseEntity<Void> sendEmail(@Valid @RequestBody EmailRequestDTO request) {
        log.info("POST /api/notifications/email - отправка email на адрес: {}",
                request.getToEmail());

        emailService.sendSimpleMessage(
                request.getToEmail(),
                request.getSubject(),
                request.getMessage()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/welcome")
    public ResponseEntity<Void> sendWelcomeEmail(@RequestParam String email,
                                                 @RequestParam String name) {
        log.info("POST /api/notifications/welcome - приветственное письмо для: {}", email);

        emailService.sendUserCreationEmail(email, name);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/deletion")
    public ResponseEntity<Void> sendDeletionEmail(@RequestParam String email,
                                                  @RequestParam String name) {
        log.info("POST /api/notifications/deletion - письмо об удалении для: {}", email);

        emailService.sendUserDeletionEmail(email, name);

        return ResponseEntity.ok().build();
    }
}
