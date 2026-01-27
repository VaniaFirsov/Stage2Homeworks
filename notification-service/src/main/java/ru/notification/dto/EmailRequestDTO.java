package ru.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmailRequestDTO {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Некорректный формат email")
    private String toEmail;

    @NotBlank(message = "Тема обязательна")
    private String subject;

    @NotBlank(message = "Сообщение обязательно")
    private String message;
}