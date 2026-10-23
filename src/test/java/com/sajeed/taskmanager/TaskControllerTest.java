package com.sajeed.taskmanager;

import com.sajeed.taskmanager.controller.TaskController;
import com.sajeed.taskmanager.dto.TaskRequest;
import com.sajeed.taskmanager.exception.GlobalExceptionHandler;
import com.sajeed.taskmanager.exception.TaskNotFoundException;
import com.sajeed.taskmanager.model.TaskPriority;
import com.sajeed.taskmanager.model.TaskStatus;
import com.sajeed.taskmanager.service.TaskService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class TaskControllerTest {

    private MockMvc mvc;
    private TaskService service;

    @BeforeEach
    void setUp() {
        service = mock(TaskService.class);

        mvc = MockMvcBuilders
            .standaloneSetup(new TaskController(service))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void blankTitleReturns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"   "}
                    """))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors.title")
                .value("Title is required"));

        verifyNoInteractions(service);
    }

    @Test
    void invalidStatusReturns400WithoutCallingService() throws Exception {
        mvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Test task","status":"INVALID"}
                    """))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void invalidIdReturns400WithoutCallingService() throws Exception {
        mvc.perform(get("/api/tasks/not-a-uuid"))
            .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void missingTaskReturns404() throws Exception {
        UUID id = UUID.randomUUID();

        when(service.findById(id))
            .thenThrow(new TaskNotFoundException(id));

        mvc.perform(get("/api/tasks/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message")
                .value("Task not found: " + id));
    }

    @Test
    void deleteReturns204WithEmptyBody() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/tasks/{id}", id))
            .andExpect(status().isNoContent())
            .andExpect(content().string(""));

        verify(service).delete(id);
    }

    @Test
    void omittedFieldsUseDefaultsOnUpdate() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(put("/api/tasks/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"title":"Updated task"}
                    """))
            .andExpect(status().isOk());

        var captor = org.mockito.ArgumentCaptor
            .forClass(TaskRequest.class);

        verify(service).update(eq(id), captor.capture());

        TaskRequest request = captor.getValue();
        assertEquals(TaskStatus.TODO, request.status());
        assertEquals(TaskPriority.MEDIUM, request.priority());
        org.junit.jupiter.api.Assertions.assertNull(request.description());
    }
}