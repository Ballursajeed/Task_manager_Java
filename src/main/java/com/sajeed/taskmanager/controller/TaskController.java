package com.sajeed.taskmanager.controller;

import com.sajeed.taskmanager.dto.TaskRequest;
import com.sajeed.taskmanager.dto.TaskResponse;
import com.sajeed.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService service;

    public TaskController(TaskService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> create(
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = service.create(request);

        return ResponseEntity
            .created(URI.create("/api/tasks/" + task.id()))
            .body(task);
    }

    @GetMapping
    public List<TaskResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public TaskResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public TaskResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}