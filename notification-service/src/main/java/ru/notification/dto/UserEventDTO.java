package ru.notification.dto;

import lombok.Data;

@Data
public class UserEventDTO {
    private String eventType;
    private String email;
    private String name;
    private Long userId;
}
