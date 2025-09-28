package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskDifficulty;
import com.example.team11project.domain.model.TaskImportance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

        String originalId = task.getId();

        databaseExecutor.execute(() -> {
            if (originalId != null && !originalId.trim().isEmpty()) {
                // Ako task već ima ID, koristi set() umesto add()
                remoteDataSource.setTaskWithId(task, originalId, new RemoteDataSource.DataSourceCallback<String>() {
                    @Override
                    public void onSuccess(String id) {
                        task.setId(originalId); // Zadrži originalni ID
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
            } else {
                // Ako nema ID, koristi add() da Firestore generiše novi
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
            }
        });
    }

    @Override
    public void getTasks(String userId, RepositoryCallback<List<Task>> callback) {

        // Korak 2: Pokreni sinhronizaciju sa Firebase-a
        remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> remoteTasks) {
                // DEO GDE PROVERAVAMO DA LI JE NEKI ZADATAK NEURADJEN
                databaseExecutor.execute(() -> {
                    List<Task> tasksToUpdateOnRemote = new ArrayList<>();
                    Date now = new Date();
                    Calendar threeDaysAgo = Calendar.getInstance();
                    threeDaysAgo.add(Calendar.DAY_OF_YEAR, -3);
                    threeDaysAgo.set(Calendar.HOUR_OF_DAY, 0); // Početak dana

                    // 1. Prođi kroz sveže podatke i proveri da li je neki istekao
                    //PROMENILA DA JE OVO SAMO ZA JEDNOKRATNE ZADATKE
                    for (Task task : remoteTasks) {
                        if (!task.isRecurring() && task.getStatus() == TaskStatus.ACTIVE &&
                                task.getExecutionTime().before(threeDaysAgo.getTime())) {

                            // Ako jeste, promeni mu status i dodaj ga u listu za ažuriranje
                            task.setStatus(TaskStatus.UNCOMPLETED);
                            tasksToUpdateOnRemote.add(task);
                        }
                    }

                    //Ažuriraj sve istekle zadatke nazad na Firebase
                    for (Task taskToUpdate : tasksToUpdateOnRemote) {
                        remoteDataSource.updateTask(taskToUpdate, new RemoteDataSource.DataSourceCallback<Void>() {
                            @Override public void onSuccess(Void result) { /* Uspeh */ }
                            @Override public void onFailure(Exception e) { /* Greška, ali nastavljamo */ }
                        });
                    }
                    // sačuvaj je celu u lokalnu bazu.
                    localDataSource.deleteAllTasksForUser(userId);
                    for (Task task : remoteTasks) {
                        localDataSource.addTask(task);
                    }


                    callback.onSuccess(remoteTasks);

                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void getTaskById(String taskId, String userId, RepositoryCallback<Task> callback) {
        databaseExecutor.execute(() -> {
            try {
                Task task = localDataSource.getTaskById(taskId, userId);
                if (task != null) {
                    callback.onSuccess(task);
                } else {
                    callback.onFailure(new Exception("Zadatak nije pronađen."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }



    @Override
    public void updateTask(Task task, RepositoryCallback<Void> callback) {
        if (task.getId() == null || task.getId().trim().isEmpty()) {
            callback.onFailure(new Exception("Zadatak nema ID i ne može biti ažuriran."));
            return;
        }
        if (task.getTitle() == null || task.getTitle().trim().isEmpty()) {
            callback.onFailure(new Exception("Naziv zadatka ne sme biti prazan."));
            return;
        }
        databaseExecutor.execute(() -> {
        remoteDataSource.updateTask(task, new RemoteDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                databaseExecutor.execute(() -> {
                    localDataSource.updateTask(task);
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
    public void getTasksInPeriod(String userId, Date startDate, Date endDate, RepositoryCallback<List<Task>> callback) {
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure(new Exception("UserID is null or empty"));
            return;
        }

        // Dohvat sa remote baze
        remoteDataSource.getTasksInPeriod(userId, startDate, endDate, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> remoteTasks) {
                databaseExecutor.execute(() -> {
                    // Ažuriraj lokalnu bazu: dodaj ili update svaki task
                    for (Task task : remoteTasks) {
                        localDataSource.updateTask(task); // updateTask treba da radi insert ako ne postoji
                    }

                    // Vrati callback sa sinhronizovanim taskovima
                    callback.onSuccess(remoteTasks);
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Ako remote fail-uje, barem vrati lokalne taskove
                databaseExecutor.execute(() -> {
                    List<Task> localTasks = localDataSource.getTasksInPeriod(userId, startDate, endDate);
                    callback.onSuccess(localTasks);
                });
            }
        });
    }




    @Override
    public void pauseTask(Task task, RepositoryCallback<Void> callback) {
        if (task.getStatus() != TaskStatus.ACTIVE) {
            callback.onFailure(new Exception("Zadatak mora biti aktivan da bi se pauzirao."));
            return;
        }
        if (!task.isRecurring()) {
            callback.onFailure(new Exception("Samo ponavljajući zadaci se mogu pauzirati."));
            return;
        }

        task.setStatus(TaskStatus.PAUSED);
        updateTask(task, callback);
    }

    @Override
    public void deleteTask(Task task, RepositoryCallback<Void> callback) {
        if (task.getStatus() != TaskStatus.ACTIVE && task.getStatus() != TaskStatus.PAUSED) {
            callback.onFailure(new Exception("Zadatak mora biti aktivan ili pauziran da bi se obrisao."));
            return;
        }

        task.setStatus(TaskStatus.DELETED);
        updateTask(task, callback);
    }

    @Override
    public void activateTask(Task task, RepositoryCallback<Void> callback) {
        // PRAVILO: Samo pauzirani zadaci se mogu ponovo aktivirati
        if (task.getStatus() != TaskStatus.PAUSED) {
            callback.onFailure(new Exception("Zadatak mora biti pauziran da bi se aktivirao."));
            return;
        }

        task.setStatus(TaskStatus.ACTIVE);
        updateTask(task, callback);
    }


    @Override
    public void countCompletedTasksByDifficulty(String userId, TaskDifficulty difficulty, Date startDate, Date endDate, RepositoryCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = localDataSource.countCompletedTasksByDifficulty(userId, difficulty, startDate, endDate);
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void countCompletedTasksByImportance(String userId, TaskImportance importance, Date startDate, Date endDate, RepositoryCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int count = localDataSource.countCompletedTasksByImportance(userId, importance, startDate, endDate);
                callback.onSuccess(count);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }



}
