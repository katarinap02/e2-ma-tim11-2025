package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskInstanceRepositoryImpl implements TaskInstanceRepository {
    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public TaskInstanceRepositoryImpl(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addTaskInstance(TaskInstance instance, RepositoryCallback<Void> callback) {
        // Validacija
        if (instance.getOriginalTaskId() == null || instance.getOriginalDate() == null) {
            callback.onFailure(new Exception("Instanca mora imati ID originalnog zadatka i datum."));
            return;
        }

        databaseExecutor.execute(() -> {
            remoteDataSource.addTaskInstance(instance, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    instance.setId(newId);

                    databaseExecutor.execute(() -> {
                        localDataSource.addTaskInstance(instance);
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
    public void getTaskInstancesForTask(String userId, String originalTaskId, RepositoryCallback<List<TaskInstance>> callback) {
        // Strategija: Uvek sinhronizuj sa servera, pa vrati lokalne.
        // Ovo osigurava da su podaci uvek sveži.
        remoteDataSource.getAllTaskInstances(userId, originalTaskId, new RemoteDataSource.DataSourceCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> remoteInstances) {
                databaseExecutor.execute(() -> {
                     localDataSource.deleteAllInstancesForTask(originalTaskId);
                    for (TaskInstance instance : remoteInstances) {
                        localDataSource.addTaskInstance(instance);
                    }

                    List<TaskInstance> localInstances = localDataSource.getAllTaskInstancesForTask(userId, originalTaskId);
                    callback.onSuccess(localInstances);
                });
            }
            @Override
            public void onFailure(Exception e) {
                databaseExecutor.execute(() -> {
                    List<TaskInstance> localInstances = localDataSource.getAllTaskInstancesForTask(userId, originalTaskId);
                    callback.onSuccess(localInstances);
                });
            }
        });
    }

    @Override
    public void updateTaskInstance(TaskInstance instance, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            remoteDataSource.updateTaskInstance(instance, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    databaseExecutor.execute(() -> {
                        localDataSource.updateTaskInstance(instance);
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
    public void deleteTaskInstance(String instanceId, String userId, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            remoteDataSource.deleteTaskInstance(instanceId, userId, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    databaseExecutor.execute(() -> {
                        localDataSource.deleteTaskInstance(instanceId);
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
    public void hasUncompletedTaskInstanceInPeriod(String userId, Date startDate, Date endDate, RepositoryCallback<Boolean> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure(new Exception("UserID is null or empty"));
            return;
        }

        remoteDataSource.getTaskInstancesInPeriod(userId, startDate, endDate, new RemoteDataSource.DataSourceCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> remoteInstances) {
                databaseExecutor.execute(() -> {
                    // Ažuriraj lokalnu bazu
                    for (TaskInstance instance : remoteInstances) {
                        localDataSource.updateTaskInstance(instance);
                    }

                    // Proveri da li postoji bar jedna koja nije completed
                    boolean exists = false;
                    for (TaskInstance instance : remoteInstances) {
                        if (instance.getNewStatus() != TaskStatus.COMPLETED && instance.getNewStatus() != TaskStatus.DELETED) {
                            exists = true;
                            break;
                        }
                    }

                    callback.onSuccess(exists);
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Ako remote fail-uje, koristi lokalne podatke
                databaseExecutor.execute(() -> {
                    List<TaskInstance> localInstances = localDataSource.getAllTaskInstancesInPeriod(userId, startDate, endDate);

                    boolean exists = false;
                    for (TaskInstance instance : localInstances) {
                        if (instance.getNewStatus() != TaskStatus.COMPLETED || instance.getNewStatus() != TaskStatus.DELETED) {
                            exists = true;
                            break;
                        }
                    }

                    callback.onSuccess(exists);
                });
            }
        });
    }





}
