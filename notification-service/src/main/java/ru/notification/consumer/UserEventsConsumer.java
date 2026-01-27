package ru.notification.consumer;

import ru.notification.dto.UserEventDTO;
import ru.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventsConsumer {

    private final EmailService emailService;

    @KafkaListener(topics = "${spring.kafka.topic.user-events}",
            groupId = "${spring.kafka.consumer.group-id}")
    public void consumeUserEvent(UserEventDTO event) {
        log.info("Получено событие из Kafka: {}", event);

        try {
            switch (event.getEventType()) {
                case "CREATE":
                    emailService.sendUserCreationEmail(event.getEmail(), event.getName());
                    break;
                case "DELETE":
                    emailService.sendUserDeletionEmail(event.getEmail(), event.getName());
                    break;
                default:
                    log.warn("Неизвестный тип события: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Ошибка при обработке события: {}", e.getMessage(), e);
        }
    }
}
