package com.emersonrt.taskmanager.util.dto;

import java.util.ArrayList;
import java.util.List;

public record ErrorMessageDTO(
        String code,
        String message,
        List<ErrorFieldDTO> fields
) {
    public ErrorMessageDTO(String code, String message) {
        this(code, message, new ArrayList<>());
    }
}
