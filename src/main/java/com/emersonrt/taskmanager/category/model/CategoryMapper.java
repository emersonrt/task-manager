package com.emersonrt.taskmanager.category.model;

import com.emersonrt.taskmanager.category.model.dto.CategoryResponseDTO;

public class CategoryMapper {
    private CategoryMapper() {
    }

    public static CategoryResponseDTO toDTO(Category category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription());
    }

}
