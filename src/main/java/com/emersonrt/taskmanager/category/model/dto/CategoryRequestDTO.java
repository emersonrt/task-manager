package com.emersonrt.taskmanager.category.model.dto;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public record CategoryRequestDTO(
        UUID id,
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
        String name,
        String description
) {
}
