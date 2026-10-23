package com.emersonrt.taskmanager.common.error;

import java.util.UUID;

/**
 * Thrown when a request body points to a related resource that does not exist.
 * The request is well formed, so this maps to 422 instead of 400 or 404.
 */
public class InvalidReferenceException extends RuntimeException {

    public InvalidReferenceException(String resource, UUID id) {
        super("%s %s does not exist".formatted(resource, id));
    }
}
