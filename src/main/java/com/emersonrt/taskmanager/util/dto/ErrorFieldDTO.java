package com.emersonrt.taskmanager.util.dto;

public record ErrorFieldDTO(
        String field,
        String message,
        String value
) {
}
