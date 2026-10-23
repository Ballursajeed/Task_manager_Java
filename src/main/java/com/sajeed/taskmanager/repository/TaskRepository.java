package com.sajeed.taskmanager.repository;

import com.sajeed.taskmanager.model.Task;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, UUID> {
}