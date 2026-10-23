package com.emersonrt.taskmanager.task;

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
