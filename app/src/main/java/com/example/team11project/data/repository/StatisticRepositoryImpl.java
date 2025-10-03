package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.StatisticRepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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

    public void getAverageTaskDifficulty(String userId, RepositoryCallback<Float> callback) {
        remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                List<Task> completed = tasks.stream()
                        .filter(t -> "COMPLETED".equals(t.getStatus().toString()))
                        .collect(Collectors.toList());

                if (completed.isEmpty()) {
                    callback.onSuccess(0f);
                    return;
                }

                float sum = 0;
                for (Task t : completed) {
                    sum += t.getDifficulty().getXpValue();
                }

                float avg = sum / completed.size();
                callback.onSuccess(avg);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void getLongestSuccessStreak(String userId, RepositoryCallback<Integer> callback) {
        remoteDataSource.getAllTasks(userId, new RemoteDataSource.DataSourceCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                if (tasks == null || tasks.isEmpty()) {
                    callback.onSuccess(0);
                    return;
                }

                Map<String, List<Task>> tasksByDate = new TreeMap<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

                for (Task t : tasks) {
                    String dateKey = sdf.format(t.getExecutionTime());
                    tasksByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(t);
                }

                int currentStreak = 0;
                int maxStreak = 0;

                for (Map.Entry<String, List<Task>> entry : tasksByDate.entrySet()) {
                    List<Task> dayTasks = entry.getValue();
                    boolean hasUncompleted = false;
                    for (Task t : dayTasks) {
                        if (t.getStatus() != TaskStatus.COMPLETED) {
                            hasUncompleted = true;
                            break;
                        }
                    }

                    if (hasUncompleted) {
                        currentStreak = 0;
                    } else {
                        currentStreak++;
                        maxStreak = Math.max(maxStreak, currentStreak);
                    }
                }

                callback.onSuccess(maxStreak);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void getUserAllianceMissionsSummary(String userId, RepositoryCallback<Map<String, Integer>> callback) {
        remoteDataSource.getAllAllianceMissions(new RemoteDataSource.DataSourceCallback<List<AllianceMission>>() {
            @Override
            public void onSuccess(List<AllianceMission> missions) {
                int started = 0;
                int finished = 0;

                if (missions != null) {
                    for (AllianceMission mission : missions) {
                        if (mission.getMemberProgress() != null) {
                            boolean isMember = mission.getMemberProgress().stream()
                                    .anyMatch(mp -> mp.getUserId().equals(userId));

                            if (isMember) {
                                if (mission.getStartDate() != null) started++;
                                if (mission.getEndDate() != null) finished++;
                            }
                        }
                    }
                }

                Map<String, Integer> summary = new HashMap<>();
                summary.put("STARTED", started);
                summary.put("FINISHED", finished);

                callback.onSuccess(summary);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


}
