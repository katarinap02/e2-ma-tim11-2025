package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.StatisticRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticRepositoryImpl implements StatisticRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;

    public StatisticRepositoryImpl(Context context){
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
    }
    @Override
    public void getTasksSummary(String userId, RepositoryCallback<Map<String, Integer>> callback) {
        remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                int totalCreated = tasks.size();
                int totalCompleted = 0;
                int totalUncompleted = 0;
                int totalCancelled = 0;

                for (Task t : tasks) {
                    switch (t.getStatus()) {
                        case COMPLETED:
                            totalCompleted++;
                            break;
                        case UNCOMPLETED:
                            totalUncompleted++;
                            break;
                        case CANCELED:
                            totalCancelled++;
                            break;
                    }
                }

                Map<String, Integer> summary = new HashMap<>();
                summary.put("CREATED", totalCreated);
                summary.put("COMPLETED", totalCompleted);
                summary.put("UNCOMPLETED", totalUncompleted);
                summary.put("CANCELED", totalCancelled);

                callback.onSuccess(summary);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getCompletedTasksPerCategory(String userId, RepositoryCallback<Map<String, Integer>> callback) {
        // Prvo dohvatimo sve kategorije korisnika
        remoteDataSource.getAllCategories(userId, new RemoteDataSource.DataSourceCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categories) {
                Map<String, String> categoryIdToName = new HashMap<>();
                for (Category c : categories) {
                    if (c != null && c.getId() != null && c.getName() != null) {
                        categoryIdToName.put(c.getId(), c.getName());
                    }
                }

                // Zatim dohvatimo sve zadatke korisnika
                remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        Map<String, Integer> completedTasksPerCategory = new HashMap<>();
                        if (tasks != null) {
                            for (Task t : tasks) {
                                if (t != null && "COMPLETED".equalsIgnoreCase(t.getStatus().toString())) {
                                    String catName = categoryIdToName.get(t.getCategoryId());
                                    if (catName != null) {
                                        completedTasksPerCategory.put(
                                                catName,
                                                completedTasksPerCategory.getOrDefault(catName, 0) + 1
                                        );
                                    }
                                }
                            }
                        }
                        callback.onSuccess(completedTasksPerCategory);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

}
