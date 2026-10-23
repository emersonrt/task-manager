package com.emersonrt.taskmanager.task;

import com.emersonrt.taskmanager.category.Category;

final class TaskMapper {

    private TaskMapper() {
    }

    static TaskResponse toResponse(Task task) {
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
