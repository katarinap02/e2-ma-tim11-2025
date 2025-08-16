package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;

import java.util.List;

public interface TaskRepository {
    void addTask(Task task, RepositoryCallback<Void> callback);

    void getTasks(String userId, RepositoryCallback<List<Task>> callback);
}
