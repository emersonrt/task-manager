package com.emersonrt.taskmanager.task;

import com.emersonrt.taskmanager.category.Category;
import com.emersonrt.taskmanager.category.CategoryRepository;
import com.emersonrt.taskmanager.common.error.InvalidReferenceException;
import com.emersonrt.taskmanager.common.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void createsTaskWithoutCategory() {
        when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(new TaskRequest("Write README", "In English", TaskStatus.TODO, null));

        assertThat(response.title()).isEqualTo("Write README");
        assertThat(response.status()).isEqualTo(TaskStatus.TODO);
        assertThat(response.categoryId()).isNull();
    }

    @Test
    void createsTaskLinkedToExistingCategory() {
        UUID categoryId = UUID.randomUUID();
        Category category = categoryWithId(categoryId);
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));
        when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.create(new TaskRequest("Deploy", null, TaskStatus.IN_PROGRESS, categoryId));

        ArgumentCaptor<Task> saved = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(saved.capture());
        assertThat(saved.getValue().getCategory()).isSameAs(category);
        assertThat(response.categoryId()).isEqualTo(categoryId);
    }

    @Test
    void rejectsUnknownCategoryOnCreate() {
        UUID categoryId = UUID.randomUUID();
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.create(new TaskRequest("Deploy", null, TaskStatus.TODO, categoryId)))
                .isInstanceOf(InvalidReferenceException.class)
                .hasMessage("Category %s does not exist".formatted(categoryId));
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void updatesEveryField() {
        UUID id = UUID.randomUUID();
        Task task = taskWithId(id, new Task("Old title", "Old", TaskStatus.TODO, categoryWithId(UUID.randomUUID())));
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        TaskResponse response = taskService.update(id, new TaskRequest("New title", "New", TaskStatus.DONE, null));

        assertThat(response.id()).isEqualTo(id);
        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.description()).isEqualTo("New");
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.categoryId()).isNull();
        verify(taskRepository).flush();
    }

    @Test
    void failsToUpdateMissingTask() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.update(id, new TaskRequest("Title", null, TaskStatus.TODO, null)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Task %s not found".formatted(id));
    }

    @Test
    void deletesExistingTask() {
        UUID id = UUID.randomUUID();
        Task task = taskWithId(id, new Task("Title", null, TaskStatus.TODO, null));
        when(taskRepository.findById(id)).thenReturn(Optional.of(task));

        taskService.delete(id);

        verify(taskRepository).delete(task);
    }

    @Test
    void failsToDeleteMissingTask() {
        UUID id = UUID.randomUUID();
        when(taskRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(id)).isInstanceOf(ResourceNotFoundException.class);
        verify(taskRepository, never()).delete(any());
    }

    @Test
    void searchPassesFiltersAndMapsThePage() {
        UUID categoryId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);
        Task task = taskWithId(UUID.randomUUID(), new Task("Title", null, TaskStatus.DONE, categoryWithId(categoryId)));
        when(taskRepository.search(TaskStatus.DONE, categoryId, pageable))
                .thenReturn(new PageImpl<>(List.of(task), pageable, 1));

        Page<TaskResponse> page = taskService.search(TaskStatus.DONE, categoryId, pageable);

        assertThat(page.getTotalElements()).isEqualTo(1);
        assertThat(page.getContent()).singleElement()
                .satisfies(response -> assertThat(response.categoryId()).isEqualTo(categoryId));
    }

    private static Category categoryWithId(UUID id) {
        Category category = new Category("Work", null);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    private static Task taskWithId(UUID id, Task task) {
        ReflectionTestUtils.setField(task, "id", id);
        return task;
    }
}
