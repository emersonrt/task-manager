package com.emersonrt.taskmanager.category.dto;

import com.emersonrt.taskmanager.category.domain.Category;

public final class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
