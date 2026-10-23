package com.emersonrt.taskmanager.category.service;

import com.emersonrt.taskmanager.category.domain.Category;
import com.emersonrt.taskmanager.category.dto.CategoryRequest;
import com.emersonrt.taskmanager.category.dto.CategoryResponse;
import com.emersonrt.taskmanager.category.out.persistence.CategoryRepository;
import com.emersonrt.taskmanager.common.error.ConflictException;
import com.emersonrt.taskmanager.common.error.ResourceNotFoundException;
import com.emersonrt.taskmanager.task.out.persistence.TaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createsCategoryWhenNameIsAvailable() {
        when(categoryRepository.existsByNameIgnoreCase("Work")).thenReturn(false);
        when(categoryRepository.saveAndFlush(any(Category.class)))
                .thenAnswer(invocation -> withId(invocation.getArgument(0), UUID.randomUUID()));

        CategoryResponse response = categoryService.create(new CategoryRequest("Work", "Office tasks"));

        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("Work");
        assertThat(response.description()).isEqualTo("Office tasks");
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        when(categoryRepository.existsByNameIgnoreCase("Work")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(new CategoryRequest("Work", null)))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Work");
        verify(categoryRepository, never()).saveAndFlush(any());
    }

    @Test
    void updatesNameAndDescription() {
        UUID id = UUID.randomUUID();
        Category category = withId(new Category("Work", "Old"), id);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Personal", id)).thenReturn(false);

        CategoryResponse response = categoryService.update(id, new CategoryRequest("Personal", "New"));

        assertThat(response).isEqualTo(new CategoryResponse(id, "Personal", "New"));
        verify(categoryRepository).flush();
    }

    @Test
    void rejectsUpdateToNameUsedByAnotherCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(withId(new Category("Work", null), id)));
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Personal", id)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(id, new CategoryRequest("Personal", null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void failsToUpdateMissingCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.update(id, new CategoryRequest("Work", null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category %s not found".formatted(id));
    }

    @Test
    void deletesCategoryWithoutTasks() {
        UUID id = UUID.randomUUID();
        Category category = withId(new Category("Work", null), id);
        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));
        when(taskRepository.existsByCategoryId(id)).thenReturn(false);

        categoryService.delete(id);

        verify(categoryRepository).delete(category);
    }

    @Test
    void refusesToDeleteCategoryWithTasks() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.of(withId(new Category("Work", null), id)));
        when(taskRepository.existsByCategoryId(id)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete(id))
                .isInstanceOf(ConflictException.class);
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void failsToDeleteMissingCategory() {
        UUID id = UUID.randomUUID();
        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.delete(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private static Category withId(Category category, UUID id) {
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }
}
