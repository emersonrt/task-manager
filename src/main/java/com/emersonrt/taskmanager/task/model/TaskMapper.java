package com.emersonrt.taskmanager.task.model;

import com.emersonrt.taskmanager.category.model.Category;
import com.emersonrt.taskmanager.task.model.dto.TaskResponseDTO;

import java.util.Optional;

public class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponseDTO toDTO(Task task) {
        return new TaskResponseDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getCreatedAt(),
                task.getUpdatedAt(),
                Optional.ofNullable(task.getCategory())
                        .map(Category::getId)
                        .orElse(null)
        );
    }

}
