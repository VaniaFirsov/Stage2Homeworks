package ru.firsov.dto;

import lombok.Data;

@Data
public class UserEventDTO {
    private String eventType; // "CREATE" или "DELETE"
    private String email;
    private String name;
    private Long userId;

    public UserEventDTO() {}

    public UserEventDTO(String eventType, String email, String name, Long userId) {
        this.eventType = eventType;
        this.email = email;
        this.name = name;
        this.userId = userId;
    }
}
