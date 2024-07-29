package com.emersonrt.taskmanager.task.service;

import com.emersonrt.taskmanager.category.model.Category;
import com.emersonrt.taskmanager.category.repository.CategoryRepository;
import com.emersonrt.taskmanager.exception.BadRequestException;
import com.emersonrt.taskmanager.exception.NotFoundException;
import com.emersonrt.taskmanager.task.model.Task;
import com.emersonrt.taskmanager.task.model.TaskMapper;
import com.emersonrt.taskmanager.task.model.dto.TaskRequestDTO;
import com.emersonrt.taskmanager.task.model.dto.TaskResponseDTO;
import com.emersonrt.taskmanager.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    private final CategoryRepository categoryRepository;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<TaskResponseDTO> getAllTasks() {
        List<Task> taskList = taskRepository.findAll();
        return taskList.stream()
                .map(TaskMapper::toDTO)
                .toList();
    }

    public TaskResponseDTO getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Task not found!"));
        return TaskMapper.toDTO(task);
    }

    @Transactional
    public TaskResponseDTO createTask(TaskRequestDTO task) throws BadRequestException {
        Category category = null;
        if (task.categoryId() != null) {
            category = categoryRepository.findById(task.categoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found!"));
        }

        Task entity = new Task(
                UUID.randomUUID(),
                task.title(),
                task.description(),
                task.status(),
                category
        );

        return TaskMapper.toDTO(taskRepository.save(entity));
    }

    @Transactional
    public TaskResponseDTO updateTask(UUID id, TaskRequestDTO updatedTask) {
        Category category = null;
        if (updatedTask.categoryId() != null) {
            category = categoryRepository.findById(updatedTask.categoryId())
                    .orElseThrow(() -> new BadRequestException("Category not found!"));
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Task not found!"));
        task.setCategory(category);
        task.setDescription(updatedTask.description());
        task.setStatus(updatedTask.status());
        task.setTitle(updatedTask.title());

        return TaskMapper.toDTO(taskRepository.saveAndFlush(task));
    }

    public void deleteTask(UUID id) {
        if (taskRepository.existsById(id)) {
            taskRepository.deleteById(id);
            return;
        }
        throw new NotFoundException("Task nof found!");
    }
}
