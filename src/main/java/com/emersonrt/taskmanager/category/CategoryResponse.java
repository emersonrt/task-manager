package com.emersonrt.taskmanager.category;

import java.util.UUID;

public record CategoryResponse(
        UUID id,
        String name,
        String description
) {
}
