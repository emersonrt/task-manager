package com.emersonrt.taskmanager.task.in.web;

import com.emersonrt.taskmanager.common.web.PageResponse;
import com.emersonrt.taskmanager.task.domain.TaskStatus;
import com.emersonrt.taskmanager.task.dto.TaskRequest;
import com.emersonrt.taskmanager.task.dto.TaskResponse;
import com.emersonrt.taskmanager.task.service.TaskService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Tasks")
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping
    public PageResponse<TaskResponse> search(@RequestParam(required = false) TaskStatus status,
                                             @RequestParam(required = false) UUID categoryId,
                                             @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return PageResponse.from(taskService.search(status, categoryId, pageable));
    }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable UUID id) {
        return taskService.findById(id);
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request) {
        TaskResponse created = taskService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.id())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable UUID id, @Valid @RequestBody TaskRequest request) {
        return taskService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
