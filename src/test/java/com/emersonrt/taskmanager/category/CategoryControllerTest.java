package com.emersonrt.taskmanager.category;

import com.emersonrt.taskmanager.common.error.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void createReturns201WithLocationHeader() throws Exception {
        UUID id = UUID.randomUUID();
        when(categoryService.create(any())).thenReturn(new CategoryResponse(id, "Work", null));

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Work"}
                        """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/categories/" + id))
                .andExpect(jsonPath("$.name").value("Work"));
    }

    @Test
    void createRejectsBlankName() throws Exception {
        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": ""}
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Name is required"));

        verifyNoInteractions(categoryService);
    }

    @Test
    void createReturns409ForDuplicateName() throws Exception {
        when(categoryService.create(any())).thenThrow(new ConflictException("A category named 'Work' already exists"));

        mockMvc.perform(post("/categories").contentType(MediaType.APPLICATION_JSON).content("""
                        {"name": "Work"}
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("A category named 'Work' already exists"));
    }

    @Test
    void deleteReturns204() throws Exception {
        mockMvc.perform(delete("/categories/{id}", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteReturns409WhenCategoryHasTasks() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ConflictException("Category %s still has tasks. Move or delete them first".formatted(id)))
                .when(categoryService).delete(id);

        mockMvc.perform(delete("/categories/{id}", id))
                .andExpect(status().isConflict());
    }
}
