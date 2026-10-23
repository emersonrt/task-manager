package com.emersonrt.taskmanager.task.dto;

import com.emersonrt.taskmanager.task.domain.TaskStatus;

import java.time.Instant;
import java.util.UUID;

public record TaskResponse(
        UUID id,
        String title,
        String description,
        TaskStatus status,
        UUID categoryId,
        Instant createdAt,
        Instant updatedAt
) {
}
