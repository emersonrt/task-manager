package com.emersonrt.taskmanager.task;

import com.emersonrt.taskmanager.category.Category;
import com.emersonrt.taskmanager.category.CategoryRepository;
import com.emersonrt.taskmanager.common.error.InvalidReferenceException;
import com.emersonrt.taskmanager.common.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaskService {

    static final String RESOURCE = "Task";

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public Page<TaskResponse> search(TaskStatus status, UUID categoryId, Pageable pageable) {
        return taskRepository.search(status, categoryId, pageable).map(TaskMapper::toResponse);
    }

    public TaskResponse findById(UUID id) {
        return TaskMapper.toResponse(getTask(id));
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Category category = resolveCategory(request.categoryId());
        Task task = new Task(request.title(), request.description(), request.status(), category);
        // Flush so the generated timestamps are part of the response
        return TaskMapper.toResponse(taskRepository.saveAndFlush(task));
    }

    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = getTask(id);
        Category category = resolveCategory(request.categoryId());
        task.update(request.title(), request.description(), request.status(), category);
        taskRepository.flush();
        return TaskMapper.toResponse(task);
    }

    @Transactional
    public void delete(UUID id) {
        taskRepository.delete(getTask(id));
    }

    private Task getTask(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }

    private Category resolveCategory(UUID categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new InvalidReferenceException("Category", categoryId));
    }
}
