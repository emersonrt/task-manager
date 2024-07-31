package com.emersonrt.taskmanager.category.service;

import com.emersonrt.taskmanager.category.model.Category;
import com.emersonrt.taskmanager.category.model.CategoryMapper;
import com.emersonrt.taskmanager.category.model.dto.CategoryRequestDTO;
import com.emersonrt.taskmanager.category.model.dto.CategoryResponseDTO;
import com.emersonrt.taskmanager.category.repository.CategoryRepository;
import com.emersonrt.taskmanager.exception.BadRequestException;
import com.emersonrt.taskmanager.exception.ConflictException;
import com.emersonrt.taskmanager.exception.NotFoundException;
import com.emersonrt.taskmanager.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private static final String CATEGORY_NOT_FOUND = "Category not found!";

    private final CategoryRepository categoryRepository;

    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categoryList = categoryRepository.findAll();
        return categoryList.stream()
                .map(CategoryMapper::toDTO)
                .toList();
    }

    public CategoryResponseDTO getCategoryById(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));
        return CategoryMapper.toDTO(category);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO category) {
        Category entity = new Category(
                UUID.randomUUID(),
                category.name(),
                category.description()
        );

        return CategoryMapper.toDTO(categoryRepository.save(entity));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(UUID id, CategoryRequestDTO updatedCategory) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException(CATEGORY_NOT_FOUND));
        category.setName(updatedCategory.name());
        category.setDescription(updatedCategory.description());

        return CategoryMapper.toDTO(categoryRepository.save(category));
    }

    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(CATEGORY_NOT_FOUND));

        if (taskRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category cannot be deleted because it has associated tasks. Please reassign or delete tasks first.");
        }

        categoryRepository.delete(category);
    }
}
