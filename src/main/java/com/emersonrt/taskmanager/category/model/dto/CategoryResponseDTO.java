package com.emersonrt.taskmanager.category.model.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record CategoryResponseDTO(
        UUID id,
        String name,
        String description
) {
}
