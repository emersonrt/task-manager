package com.emersonrt.taskmanager.common.error;

import java.util.UUID;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, UUID id) {
        super("%s %s not found".formatted(resource, id));
    }
}
