package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.TaskInstance;

import java.util.List;

public interface TaskInstanceRepository {
    void addTaskInstance(TaskInstance instance, RepositoryCallback<Void> callback);

    void getTaskInstancesForTask(String userId, String originalTaskId, RepositoryCallback<List<TaskInstance>> callback);

    void updateTaskInstance(TaskInstance instance, RepositoryCallback<Void> callback);

    void deleteTaskInstance(String instanceId, String userId, RepositoryCallback<Void> callback);
}
