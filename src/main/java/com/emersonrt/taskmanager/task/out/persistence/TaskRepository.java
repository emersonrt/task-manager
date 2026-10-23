package com.emersonrt.taskmanager.task.out.persistence;

import com.emersonrt.taskmanager.task.domain.Task;
import com.emersonrt.taskmanager.task.domain.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    boolean existsByCategoryId(UUID categoryId);

    /**
     * Optional filters: a null parameter means "do not filter by this field".
     */
    @Query("""
            SELECT t FROM Task t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:categoryId IS NULL OR t.category.id = :categoryId)
            """)
    Page<Task> search(@Param("status") TaskStatus status,
                      @Param("categoryId") UUID categoryId,
                      Pageable pageable);
}
