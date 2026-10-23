package com.emersonrt.taskmanager.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
        @NotBlank(message = "Name is required")
        @Size(max = 50, message = "Name must have at most 50 characters")
        String name,

        @Size(max = 255, message = "Description must have at most 255 characters")
        String description
) {
}
