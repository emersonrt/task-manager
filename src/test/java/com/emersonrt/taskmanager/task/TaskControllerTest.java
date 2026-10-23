package com.emersonrt.taskmanager.task;

import com.emersonrt.taskmanager.common.error.InvalidReferenceException;
import com.emersonrt.taskmanager.common.error.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * HTTP contract of the tasks endpoints: status codes, validation and error format.
 * The service is mocked, so these tests stay fast and focused on the web layer.
 */
@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final MediaType PROBLEM_JSON = MediaType.APPLICATION_PROBLEM_JSON;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.create(any())).thenReturn(response(id));

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Write tests", "status": "TODO"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/tasks/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    void createRejectsInvalidBodyWithFieldErrors() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": " ", "description": "%s"}
                        """.formatted("x".repeat(501))))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON.toString()))
                .andExpect(jsonPath("$.detail").value("Request validation failed"))
                .andExpect(jsonPath("$.errors.length()").value(3))
                .andExpect(jsonPath("$.errors[?(@.field == 'title')].message").value("Title is required"))
                .andExpect(jsonPath("$.errors[?(@.field == 'status')].message").value("Status is required"))
                .andExpect(jsonPath("$.errors[?(@.field == 'description')].message")
                        .value("Description must have at most 500 characters"));

        verifyNoInteractions(taskService);
    }

    @Test
    void createRejectsUnknownStatusValue() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Write tests", "status": "BLOCKED"}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON.toString()));

        verifyNoInteractions(taskService);
    }

    @Test
    void createReturns422WhenCategoryDoesNotExist() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(taskService.create(any())).thenThrow(new InvalidReferenceException("Category", categoryId));

        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Write tests", "status": "TODO", "categoryId": "%s"}
                        """.formatted(categoryId)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.detail").value("Category %s does not exist".formatted(categoryId)));
    }

    @Test
    void findByIdReturns404WhenTaskDoesNotExist() throws Exception {
        UUID id = UUID.randomUUID();
        when(taskService.findById(id)).thenThrow(new ResourceNotFoundException("Task", id));

        mockMvc.perform(get("/tasks/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(header().string("Content-Type", PROBLEM_JSON.toString()))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Task %s not found".formatted(id)));
    }

    @Test
    void findByIdRejectsMalformedId() throws Exception {
        mockMvc.perform(get("/tasks/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Type", PROBLEM_JSON.toString()));

        verifyNoInteractions(taskService);
    }

    @Test
    void updateRejectsMalformedJson() throws Exception {
        mockMvc.perform(put("/tasks/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON).content("{"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(taskService);
    }

    @Test
    void searchPassesFiltersAndReturnsPageEnvelope() throws Exception {
        UUID categoryId = UUID.randomUUID();
        when(taskService.search(eq(TaskStatus.DONE), eq(categoryId), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(response(UUID.randomUUID()))));

        mockMvc.perform(get("/tasks").param("status", "DONE").param("categoryId", categoryId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.page").value(0));
    }

    @Test
    void searchWithoutFiltersPassesNulls() throws Exception {
        when(taskService.search(isNull(), isNull(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));

        verify(taskService).search(isNull(), isNull(), any(Pageable.class));
    }

    private static TaskResponse response(UUID id) {
        Instant now = Instant.parse("2026-01-01T10:00:00Z");
        return new TaskResponse(id, "Write tests", null, TaskStatus.TODO, null, now, now);
    }
}
