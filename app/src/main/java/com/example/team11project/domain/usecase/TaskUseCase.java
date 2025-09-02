package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskDifficulty;
import com.example.team11project.domain.model.TaskImportance;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import java.util.Calendar;
import java.util.Date;

public class TaskUseCase {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final LevelInfoRepository levelInfoRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    public TaskUseCase(TaskRepository taskRepository, UserRepository userRepository, TaskInstanceRepository taskInstanceRepository, LevelInfoRepository levelInfoRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.levelInfoRepository = levelInfoRepository;
    }

    public void completeTask(Task task, String userId, Date instanceDate, RepositoryCallback<Integer> finalCallback) {
        if (task.getStatus() != TaskStatus.ACTIVE) {
            finalCallback.onFailure(new Exception("Samo aktivni zadaci se mogu označiti kao završeni."));
            return;
        }

        if (!task.isRecurring()) {
            // Regularni zadatak
            completeRegularTask(task, userId, finalCallback);
        } else {
            // Ponavljajući zadatak
            if (instanceDate == null) {
                finalCallback.onFailure(new Exception("Datum instance je obavezan za ponavljajuće zadatke."));
                return;
            }
            completeRecurringTask(task, userId, instanceDate, finalCallback);
        }
    }

    public void completeRegularTask(Task task, String userId, RepositoryCallback<Integer> finalCallback) {
        // Koristimo AtomicInteger da bismo bezbedno sabirali XP sa različitih thread-ova
        AtomicInteger calculatedXp = new AtomicInteger(0);

        // Izračunavanje XP za težinu
        calculateXpForDifficulty(task, userId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer difficultyXp) {
                calculatedXp.addAndGet(difficultyXp);

                // Izračunavanje XP za bitnost
                calculateXpForImportance(task, userId, new RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer importanceXp) {
                        calculatedXp.addAndGet(importanceXp);
                        task.setStatus(TaskStatus.COMPLETED);
                        task.setCompletionDate(new Date());

                        taskRepository.updateTask(task, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                // Dodeli XP korisniku
                                addXpToUser(userId, calculatedXp.get(), finalCallback);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                finalCallback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        finalCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                finalCallback.onFailure(e);
            }
        });
    }

    private void completeRecurringTask(Task task, String userId, Date instanceDate, RepositoryCallback<Integer> finalCallback) {
        // Validacija da nije već kompletirana ili otkazana
        taskInstanceRepository.getTaskInstancesForTask(userId, task.getId(), new RepositoryCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> instances) {
                Log.d("CompleteTask", "Got " + instances.size() + " existing instances");

                // Proveri da li već postoji izuzetak za dati dan
                for (TaskInstance instance : instances) {
                    if (isSameDay(instance.getOriginalDate(), instanceDate)) {
                        if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                                instance.getNewStatus() == TaskStatus.CANCELED) {
                            finalCallback.onFailure(new Exception("Zadatak je već završen ili otkazan za taj datum."));
                            return; // Prekini operaciju
                        }
                    }
                }

                // Izračunavanje XP za ponavljajući zadatak
                AtomicInteger calculatedXp = new AtomicInteger(0);

                calculateXpForDifficulty(task, userId, new RepositoryCallback<Integer>() {
                    @Override
                    public void onSuccess(Integer difficultyXp) {
                        calculatedXp.addAndGet(difficultyXp);

                        calculateXpForImportance(task, userId, new RepositoryCallback<Integer>() {
                            @Override
                            public void onSuccess(Integer importanceXp) {
                                calculatedXp.addAndGet(importanceXp);

                                TaskInstance taskInstance = new TaskInstance();
                                taskInstance.setOriginalTaskId(task.getId());
                                taskInstance.setUserId(userId);
                                taskInstance.setNewStatus(TaskStatus.COMPLETED);
                                taskInstance.setOriginalDate(getStartOfDay(instanceDate));
                                taskInstance.setCompletionDate(new Date());

                                Log.d("CompleteTask", "TaskInstance created: " + taskInstance.toString());

                                taskInstanceRepository.addTaskInstance(taskInstance, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        // Dodeli XP korisniku
                                        addXpToUser(userId, calculatedXp.get(), finalCallback);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        finalCallback.onFailure(e);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                finalCallback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        finalCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Greška pri dohvatanju postojećih instanci
                finalCallback.onFailure(e);
            }
        });
    }


    private void addXpToUser(String userId, int xpAmount, RepositoryCallback<Integer> finalCallback) {
        if (xpAmount > 0) {
            userRepository.getUserById(userId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    levelInfoRepository.addXp(user, xpAmount, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            finalCallback.onSuccess(xpAmount);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            finalCallback.onFailure(e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    finalCallback.onFailure(e);
                }
            });
        } else {
            // Ako je osvojeno 0 poena, samo javi uspeh bez dodavanja XP
            finalCallback.onSuccess(0);
        }
    }


    private void calculateXpForDifficulty(Task task, String userId, RepositoryCallback<Integer> callback) {
        TaskDifficulty difficulty = task.getDifficulty();
        Date todayStart = getStartOfDay(new Date());
        Date todayEnd = getEndOfDay(new Date());
        Date weekStart = getStartOfWeek(new Date());
        Date weekEnd = getEndOfWeek(new Date());

        // Primer za dnevnu kvotu
        if (difficulty == TaskDifficulty.VERY_EASY || difficulty == TaskDifficulty.EASY || difficulty == TaskDifficulty.HARD) {
            int dailyLimit = (difficulty == TaskDifficulty.HARD) ? 2 : 5;

            taskRepository.countCompletedTasksByDifficulty(userId, difficulty, todayStart, todayEnd, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    if (count < dailyLimit) {
                        callback.onSuccess(difficulty.getXpValue());
                    } else {
                        callback.onSuccess(0);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        } else if (difficulty == TaskDifficulty.EXTREME) {
            int weekLimit = 1;
            taskRepository.countCompletedTasksByDifficulty(userId, difficulty, weekStart, weekEnd, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    if (count < weekLimit) {
                        callback.onSuccess(difficulty.getXpValue());
                    } else {
                        callback.onSuccess(0);
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });

        }
        else {
            callback.onSuccess(difficulty.getXpValue()); // Za sada, ako nema kvote, dodeli XP
        }
    }

    private void calculateXpForImportance(Task task, String userId, RepositoryCallback<Integer> callback) {

        TaskImportance importance = task.getImportance();
        Date todayStart = getStartOfDay(new Date());
        Date todayEnd = getEndOfDay(new Date());
        Date monthStart = getStartOfMonth(new Date());
        Date monthEnd = getEndOfMonth(new Date());

        if (importance == TaskImportance.NORMAL || importance == TaskImportance.IMPORTANT || importance == TaskImportance.VERY_IMPORTANT) {
            int dailyLimit = (importance == TaskImportance.VERY_IMPORTANT) ? 2 : 5;

            taskRepository.countCompletedTasksByImportance(userId, importance, todayStart, todayEnd, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    if (count < dailyLimit) {
                        callback.onSuccess(importance.getXpValue());
                    } else {
                        callback.onSuccess(0);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        } else if (importance== TaskImportance.SPECIAL) {
            int monthLimit = 1;
            taskRepository.countCompletedTasksByImportance(userId, importance, monthStart, monthEnd, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    if (count < monthLimit) {
                        callback.onSuccess(importance.getXpValue());
                    } else {
                        callback.onSuccess(0);
                    }
                }
                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });

        }
        else {
            callback.onSuccess(importance.getXpValue()); // Za sada, ako nema kvote, dodeli XP
        }
    }


    // Pomocne metode za rad sa vremenom
    private Date getStartOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getEndOfDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar.getTime();

    }

    private Date getStartOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        // Postavi kalendar na prvi dan u nedelji (npr. Ponedeljak)
        // Calendar.MONDAY, Calendar.TUESDAY...
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        // Zatim vrati vreme na početak tog dana
        return getStartOfDay(calendar.getTime());
    }

    private Date getEndOfWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(getStartOfWeek(date));
        calendar.add(Calendar.DAY_OF_WEEK, 6);
        return getEndOfDay(calendar.getTime());
    }

    private Date getStartOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
    }

    private Date getEndOfMonth(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int lastDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, lastDay);
        return getEndOfDay(calendar.getTime());
    }

    public void cancelTask(Task task, String userId, Date instanceDate, RepositoryCallback<Void> finalCallback)
    {
        if (task.getStatus() != TaskStatus.ACTIVE) {
            finalCallback.onFailure(new Exception("Samo aktivni zadaci se mogu otkazati."));
            return;
        }

        if(!task.isRecurring())
        {
            task.setStatus(TaskStatus.CANCELED);
            taskRepository.updateTask(task, finalCallback);
        }
        else {
            //validacija da nije vec kompletirana ili otkazana
            taskInstanceRepository.getTaskInstancesForTask(userId, task.getId(), new RepositoryCallback<List<TaskInstance>>() {
                        @Override
                        public void onSuccess(List<TaskInstance> instances) {
                            Log.d("CancelTask", "Got " + instances.size() + " existing instances");

                            // Korak 2: Proveri da li već postoji izuzetak za dati dan
                            for (TaskInstance instance : instances) {
                                if (isSameDay(instance.getOriginalDate(), instanceDate)) {
                                    if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                                            instance.getNewStatus() == TaskStatus.CANCELED) {

                                        // Ako je već završen ili otkazan, ne može se ponovo otkazati.
                                        finalCallback.onFailure(new Exception("Zadatak je već završen ili otkazan."));
                                        return; // Prekini operaciju
                                    }
                                }
                            }


                            TaskInstance taskInstance = new TaskInstance();
                            taskInstance.setOriginalTaskId(task.getId());
                            taskInstance.setUserId(userId);
                            taskInstance.setNewStatus(TaskStatus.CANCELED);
                            taskInstance.setOriginalDate(getStartOfDay(instanceDate));
                            taskInstance.setCompletionDate(new Date());

                            Log.d("CancelTask", "TaskInstance created: " + taskInstance.toString());

                            taskInstanceRepository.addTaskInstance(taskInstance, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    // Javi da je operacija uspešno završena
                                    finalCallback.onSuccess(null);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    finalCallback.onFailure(e);
                                }
                            });
                        }
                @Override
                public void onFailure(Exception e) {
                    // Greška pri dohvatanju postojećih instanci
                    finalCallback.onFailure(e);
                }
            });
        }

    }

    private boolean isSameDay(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        Calendar c1 = Calendar.getInstance();
        c1.setTime(d1);
        Calendar c2 = Calendar.getInstance();
        c2.setTime(d2);

        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }
}
