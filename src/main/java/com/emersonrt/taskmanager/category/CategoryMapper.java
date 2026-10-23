package com.emersonrt.taskmanager.category;

final class CategoryMapper {

    private CategoryMapper() {
    }

    static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getDescription());
    }
}
