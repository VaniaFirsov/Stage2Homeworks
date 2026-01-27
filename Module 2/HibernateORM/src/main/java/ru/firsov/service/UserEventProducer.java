package ru.firsov.service;

import ru.firsov.dto.UserEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${spring.kafka.topic.user-events}")
    private String userEventsTopic;

    public void sendUserEvent(String eventType, String email, String name, Long userId) {
        UserEventDTO event = new UserEventDTO(eventType, email, name, userId);

        kafkaTemplate.send(userEventsTopic, event)
                .whenComplete((result, ex) -> {
                    if (ex == null) {
                        log.info("Сообщение отправлено в топик {}: {}", userEventsTopic, event);
                    } else {
                        log.error("Ошибка при отправке сообщения в Kafka", ex);
                    }
                });
    }
}