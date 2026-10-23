package com.emersonrt.taskmanager.task;

import com.emersonrt.taskmanager.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class TaskApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createdTaskCanBeFetchedFromItsLocation() throws Exception {
        String location = mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Write README", "description": "In English", "status": "TODO"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.createdAt").isNotEmpty())
                .andExpect(jsonPath("$.updatedAt").isNotEmpty())
                .andReturn().getResponse().getHeader("Location");

        assertThat(location).isNotNull();
        mockMvc.perform(get(location))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Write README"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.categoryId").doesNotExist());
    }

    @Test
    void updateReplacesFieldsAndRefreshesUpdatedAt() throws Exception {
        String categoryId = createCategory("Work");
        String created = createTask("Draft", "TODO", null);
        String id = JsonPath.read(created, "$.id");
        Instant createdAt = Instant.parse(JsonPath.read(created, "$.createdAt"));

        String updated = mockMvc.perform(put("/tasks/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Final", "status": "DONE", "categoryId": "%s"}
                        """.formatted(categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Final"))
                .andExpect(jsonPath("$.status").value("DONE"))
                .andExpect(jsonPath("$.categoryId").value(categoryId))
                .andReturn().getResponse().getContentAsString();

        assertThat(Instant.parse(JsonPath.read(updated, "$.createdAt"))).isEqualTo(createdAt);
        assertThat(Instant.parse(JsonPath.read(updated, "$.updatedAt"))).isAfterOrEqualTo(createdAt);
    }

    @Test
    void filtersByStatusAndCategory() throws Exception {
        String work = createCategory("Work");
        String home = createCategory("Home");
        createTask("Ship release", "DONE", work);
        createTask("Plan sprint", "TODO", work);
        createTask("Fix sink", "DONE", home);
        createTask("No category", "DONE", null);

        mockMvc.perform(get("/tasks").param("status", "DONE").param("categoryId", work))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Ship release"));

        mockMvc.perform(get("/tasks").param("status", "DONE"))
                .andExpect(jsonPath("$.totalElements").value(3));

        mockMvc.perform(get("/tasks"))
                .andExpect(jsonPath("$.totalElements").value(4));
    }

    @Test
    void pageSizeIsCappedAt100() throws Exception {
        mockMvc.perform(get("/tasks").param("size", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void returns422ForUnknownCategory() throws Exception {
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Orphan", "status": "TODO", "categoryId": "%s"}
                        """.formatted(UUID.randomUUID())))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(header().string("Content-Type", MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void returns404ForMissingTask() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(get("/tasks/{id}", id)).andExpect(status().isNotFound());
        mockMvc.perform(put("/tasks/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Ghost", "status": "TODO"}
                        """))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/tasks/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    void deletedTaskIsGone() throws Exception {
        String id = JsonPath.read(createTask("Temporary", "TODO", null), "$.id");

        mockMvc.perform(delete("/tasks/{id}", id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/tasks/{id}", id)).andExpect(status().isNotFound());
    }

    private String createCategory(String name) throws Exception {
        String response = mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "%s"}
                        """.formatted(name)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }

    private String createTask(String title, String status, String categoryId) throws Exception {
        String category = categoryId == null ? "null" : "\"%s\"".formatted(categoryId);
        return mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "%s", "status": "%s", "categoryId": %s}
                        """.formatted(title, status, category)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
