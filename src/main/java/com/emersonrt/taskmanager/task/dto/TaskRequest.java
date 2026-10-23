package com.emersonrt.taskmanager.task.dto;

import com.emersonrt.taskmanager.task.domain.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TaskRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 100, message = "Title must have at most 100 characters")
        String title,

        @Size(max = 500, message = "Description must have at most 500 characters")
        String description,

        @NotNull(message = "Status is required")
        TaskStatus status,

        UUID categoryId
) {
}
