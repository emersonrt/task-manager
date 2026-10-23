package com.emersonrt.taskmanager.common.web;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable JSON shape for paginated responses, so the API contract does not depend on
 * Spring Data's internal Page serialization.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}
