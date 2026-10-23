package com.emersonrt.taskmanager.category;

import com.emersonrt.taskmanager.support.IntegrationTest;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTest
class CategoryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fullLifecycle() throws Exception {
        String id = createCategory("Work", "Office tasks");

        mockMvc.perform(get("/categories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Work"))
                .andExpect(jsonPath("$.description").value("Office tasks"));

        mockMvc.perform(put("/categories/{id}", id).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Job", "description": "Renamed"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Job"));

        mockMvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/categories/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void listsCategoriesSortedByNameWithPagination() throws Exception {
        createCategory("Personal", null);
        createCategory("Errands", null);
        createCategory("Work", null);

        mockMvc.perform(get("/categories").param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Errands"))
                .andExpect(jsonPath("$.content[1].name").value("Personal"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void namesAreUniqueIgnoringCase() throws Exception {
        createCategory("Work", null);

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "WORK"}
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotRenameToAnExistingName() throws Exception {
        createCategory("Work", null);
        String personalId = createCategory("Personal", null);

        mockMvc.perform(put("/categories/{id}", personalId).contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "work"}
                        """))
                .andExpect(status().isConflict());
    }

    @Test
    void cannotDeleteCategoryThatStillHasTasks() throws Exception {
        String categoryId = createCategory("Work", null);
        mockMvc.perform(post("/tasks").contentType(MediaType.APPLICATION_JSON).content("""
                        {"title": "Prepare demo", "status": "TODO", "categoryId": "%s"}
                        """.formatted(categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/categories/{id}", categoryId))
                .andExpect(status().isConflict());
    }

    private String createCategory(String name, String description) throws Exception {
        String body = description == null
                ? """
                  {"name": "%s"}
                  """.formatted(name)
                : """
                  {"name": "%s", "description": "%s"}
                  """.formatted(name, description);

        String response = mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(response, "$.id");
    }
}
