package com.emersonrt.taskmanager.category.model.dto;

import java.util.UUID;

public record CategoryResponseDTO(
        UUID id,
        String name,
        String description
) {
}
