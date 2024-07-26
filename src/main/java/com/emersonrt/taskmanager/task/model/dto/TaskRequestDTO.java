package com.emersonrt.taskmanager.task.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskRequestDTO(
        UUID id,
        @NotBlank(message = "Title cannot be blank")
        @Size(min = 1, max = 100, message = "Title must be between 1 and 100 characters")
        String title,
        @Size(max = 500, message = "Description cannot exceed 500 characters")
        String description,
        @NotBlank(message = "Status cannot be blank")
        @Size(min = 1, max = 50, message = "Status must be between 1 and 50 characters")
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        UUID categoryId
) {
}
