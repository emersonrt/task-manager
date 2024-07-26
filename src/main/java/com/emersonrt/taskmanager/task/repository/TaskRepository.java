package com.emersonrt.taskmanager.task.repository;

import com.emersonrt.taskmanager.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    boolean existsByCategoryId(UUID categoryId);
}
