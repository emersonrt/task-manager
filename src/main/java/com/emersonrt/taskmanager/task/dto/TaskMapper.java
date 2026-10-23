package com.emersonrt.taskmanager.task.dto;

import com.emersonrt.taskmanager.category.domain.Category;
import com.emersonrt.taskmanager.task.domain.Task;

public final class TaskMapper {

    private TaskMapper() {
    }

    public static TaskResponse toResponse(Task task) {
        Category category = task.getCategory();
        return new TaskResponse(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                category == null ? null : category.getId(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
