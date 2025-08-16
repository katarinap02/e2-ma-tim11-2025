package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepositoryImpl implements TaskRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public TaskRepositoryImpl(Context context)
    {
        localDataSource = new LocalDataSource(context);
        remoteDataSource = new RemoteDataSource();
        databaseExecutor = Executors.newSingleThreadExecutor();
    }
    @Override
    public void addTask(Task task, RepositoryCallback<Void> callback) {
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            callback.onFailure(new Exception("Naziv zadatka ne sme biti prazan."));
            return;
        }
        databaseExecutor.execute(() -> {
            // Korak 1: Pokušaj da dodaš na Firebase
            remoteDataSource.addTask(task, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    task.setId(newId);

                    databaseExecutor.execute(() -> {
                        localDataSource.addTask(task);
                        callback.onSuccess(null);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        });
    }

    @Override
    public void getTasks(String userId, RepositoryCallback<List<Task>> callback) {
        databaseExecutor.execute(() -> {
            List<Task> localTasks = localDataSource.getAllTasks(userId);
        });

        // Korak 2: Pokreni sinhronizaciju sa Firebase-a
        remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> remoteTasks) {
                // Dobili smo sveže podatke, sada ih sinhronizuj sa lokalnom bazom
                databaseExecutor.execute(() -> {
                    localDataSource.deleteAllTasksForUser(userId);
                    for (Task cat : remoteTasks) {
                        localDataSource.addTask(cat);
                    }
                    // Nakon sinhronizacije, ponovo pošalji sveže podatke UI-ju
                    List<Task> freshLocalTasks = localDataSource.getAllTasks(userId);
                    callback.onSuccess(freshLocalTasks);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }
}
