package com.sajeed.taskmanager.service;

import com.sajeed.taskmanager.dto.TaskRequest;
import com.sajeed.taskmanager.dto.TaskResponse;
import com.sajeed.taskmanager.exception.TaskNotFoundException;
import com.sajeed.taskmanager.model.Task;
import com.sajeed.taskmanager.repository.TaskRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TaskResponse create(TaskRequest request) {
        Task task = new Task(
            request.title(),
            request.description(),
            request.status(),
            request.priority()
        );

        return TaskResponse.from(repository.saveAndFlush(task));
    }

    public List<TaskResponse> findAll() {
        return repository.findAll(
                Sort.by(Sort.Direction.DESC, "createdAt", "id")
            )
            .stream()
            .map(TaskResponse::from)
            .toList();
    }

    public TaskResponse findById(UUID id) {
        return TaskResponse.from(requireTask(id));
    }

    @Transactional
    public TaskResponse update(UUID id, TaskRequest request) {
        Task task = requireTask(id);

        task.replaceDetails(
            request.title(),
            request.description(),
            request.status(),
            request.priority()
        );

        repository.flush();
        return TaskResponse.from(task);
    }

    @Transactional
    public void delete(UUID id) {
        Task task = requireTask(id);
        repository.delete(task);
    }

    private Task requireTask(UUID id) {
        return repository.findById(id)
            .orElseThrow(() -> new TaskNotFoundException(id));
    }
}