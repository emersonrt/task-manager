package com.emersonrt.taskmanager.category.service;

import com.emersonrt.taskmanager.category.domain.Category;
import com.emersonrt.taskmanager.category.dto.CategoryMapper;
import com.emersonrt.taskmanager.category.dto.CategoryRequest;
import com.emersonrt.taskmanager.category.dto.CategoryResponse;
import com.emersonrt.taskmanager.category.out.persistence.CategoryRepository;
import com.emersonrt.taskmanager.common.error.ConflictException;
import com.emersonrt.taskmanager.common.error.ResourceNotFoundException;
import com.emersonrt.taskmanager.task.out.persistence.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private static final String RESOURCE = "Category";

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;

    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
    }

    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(CategoryMapper::toResponse);
    }

    public CategoryResponse findById(UUID id) {
        return CategoryMapper.toResponse(getCategory(id));
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.name())) {
            throw duplicateName(request.name());
        }
        Category category = categoryRepository.saveAndFlush(new Category(request.name(), request.description()));
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public CategoryResponse update(UUID id, CategoryRequest request) {
        Category category = getCategory(id);
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.name(), id)) {
            throw duplicateName(request.name());
        }
        category.update(request.name(), request.description());
        // Flush inside the repository so constraint violations surface as translated exceptions
        categoryRepository.flush();
        return CategoryMapper.toResponse(category);
    }

    @Transactional
    public void delete(UUID id) {
        Category category = getCategory(id);
        if (taskRepository.existsByCategoryId(id)) {
            throw new ConflictException("Category %s still has tasks. Move or delete them first".formatted(id));
        }
        categoryRepository.delete(category);
        categoryRepository.flush();
    }

    private Category getCategory(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }

    private static ConflictException duplicateName(String name) {
        return new ConflictException("A category named '%s' already exists".formatted(name));
    }
}
