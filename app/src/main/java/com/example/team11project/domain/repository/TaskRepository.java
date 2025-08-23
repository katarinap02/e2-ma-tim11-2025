package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskDifficulty;
import com.example.team11project.domain.model.TaskImportance;

import java.util.Date;
import java.util.List;

public interface TaskRepository {
    void addTask(Task task, RepositoryCallback<Void> callback);

    void getTasks(String userId, RepositoryCallback<List<Task>> callback);

    void getTaskById(String taskId, String userId, RepositoryCallback<Task> callback);

    void updateTask(Task task, RepositoryCallback<Void> callback);

    void pauseTask(Task task, RepositoryCallback<Void> callback);
    void activateTask(Task task, RepositoryCallback<Void> callback);
    void cancelTask(Task task, RepositoryCallback<Void> callback);

    void countCompletedTasksByDifficulty(String userId, TaskDifficulty difficulty, Date startDate, Date endDate, RepositoryCallback<Integer> callback);
    void countCompletedTasksByImportance(String userId, TaskImportance importance, Date startDate, Date endDate, RepositoryCallback<Integer> callback);
}
