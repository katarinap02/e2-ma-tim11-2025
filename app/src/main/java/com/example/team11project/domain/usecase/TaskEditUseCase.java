package com.example.team11project.domain.usecase;

import android.util.Log;

import com.example.team11project.domain.model.RecurrenceUnit;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

public class TaskEditUseCase {

    private final TaskRepository taskRepository;
    private final TaskInstanceRepository taskInstanceRepository;

    public TaskEditUseCase(TaskRepository taskRepository, TaskInstanceRepository taskInstanceRepository) {
        this.taskRepository = taskRepository;
        this.taskInstanceRepository = taskInstanceRepository;
    }

    public void editTask(Task originalTask, Task editedTask, String userId, Date instanceDate,
                         RepositoryCallback<List<Task>> finalCallback) {

        if (originalTask == null || editedTask == null || userId == null) {
            finalCallback.onFailure(new Exception("Neispravni parametri za izmenu zadatka."));
            return;
        }

        // Proveri dozvole za izmenu
        if (!canEditTask(originalTask, instanceDate)) {
            finalCallback.onFailure(new Exception("Zadatak ne može biti izmenjen u ovom stanju."));
            return;
        }

        if (!originalTask.isRecurring()) {
            // Jednostavna izmena jednokratnog zadatka
            editSingleTask(originalTask, editedTask, finalCallback);
        } else {
            // Za recurring zadatke - nova jednostavna logika
            editRecurringTaskSimplified(originalTask, editedTask, userId, instanceDate, finalCallback);
        }
    }

    private void editRecurringTaskSimplified(Task originalTask, Task editedTask, String userId,
                                             Date instanceDate, RepositoryCallback<List<Task>> finalCallback) {

        Log.d("TaskEdit", "Starting simplified recurring task edit for: " + originalTask.getId());

        // Prvo učitaj postojeće instance
        taskInstanceRepository.getTaskInstancesForTask(userId, originalTask.getId(), new RepositoryCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> instances) {
                processRecurringTaskEdit(originalTask, editedTask, userId, instanceDate, instances, finalCallback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error loading task instances", e);
                finalCallback.onFailure(e);
            }
        });
    }

    private void processRecurringTaskEdit(Task originalTask, Task editedTask, String userId,
                                          Date instanceDate, List<TaskInstance> instances,
                                          RepositoryCallback<List<Task>> finalCallback) {

        Date today = getStartOfDay(new Date());
        Date startDate = getStartOfDay(originalTask.getRecurrenceStartDate());
        Date endDate = getStartOfDay(originalTask.getRecurrenceEndDate());

        Log.d("TaskEdit", "Processing edit - Today: " + today + ", Start: " + startDate + ", End: " + endDate);

        List<Task> resultTasks = new ArrayList<>();

        //Kreira task za prošlost (ako postoji)
        Task pastTask = null;
        if (startDate.before(today)) {
            Date pastEnd = getPreviousDay(today);

            pastTask = cloneTask(originalTask);
            pastTask.setRecurrenceStartDate(startDate);
            pastTask.setRecurrenceEndDate(pastEnd);
            resultTasks.add(pastTask);
        }

        //Kreira task za budućnost/sadašnjost (zadržava originalni ID)
        Date futureStart = findNextValidRecurrenceDate(originalTask, today);
        Task futureTask = null;

        //Kreira taskove za completed/canceled instance
        List<Task> instanceTasks = createTasksForSpecialInstances(instances, originalTask, resultTasks);
        resultTasks.addAll(instanceTasks);

        if (futureStart != null && !futureStart.after(endDate)) {
            // Primeni izmene na originalni task (zadržava ID)
            applyEditsToTask(originalTask, editedTask);
            originalTask.setRecurrenceStartDate(futureStart);
            originalTask.setRecurrenceEndDate(endDate);

            futureTask = originalTask;
            resultTasks.add(futureTask);

        }

        //Sačuvaj sve taskove
        saveTasksAndUpdateInstances(originalTask.getId(), resultTasks, instances, pastTask,
                futureTask, instanceTasks, finalCallback);
    }

    private List<Task> createTasksForSpecialInstances(List<TaskInstance> instances, Task originalTask,
                                                      List<Task> resultTasks) {
        List<Task> instanceTasks = new ArrayList<>();

        if (instances != null) {
            for (TaskInstance instance : instances) {
                if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                        instance.getNewStatus() == TaskStatus.CANCELED) {

                    Date instanceDate = getStartOfDay(instance.getOriginalDate());

                    // Proveri da li već postoji task koji pokriva ovaj datum
                    boolean covered = false;
                    for (Task existingTask : resultTasks) {
                        if (dateInRange(instanceDate, existingTask.getRecurrenceStartDate(),
                                existingTask.getRecurrenceEndDate()) &&
                                isValidRecurrenceDate(existingTask, instanceDate)) {
                            covered = true;
                            break;
                        }
                    }

                    if (!covered) {
                        // Kreiraj poseban task za ovaj datum
                        Task instanceTask = cloneTask(originalTask);
                        instanceTask.setRecurrenceStartDate(instanceDate);
                        instanceTask.setRecurrenceEndDate(instanceDate);

                        instanceTasks.add(instanceTask);

                        Log.d("TaskEdit", "Created instance task for " + instance.getNewStatus() +
                                " on " + instanceDate + ": " + instanceTask.getId());
                    }
                }
            }
        }

        return instanceTasks;
    }

    private void saveTasksAndUpdateInstances(String originalTaskId, List<Task> allTasks,
                                             List<TaskInstance> instances, Task pastTask,
                                             Task futureTask, List<Task> instanceTasks,
                                             RepositoryCallback<List<Task>> finalCallback) {

        Log.d("TaskEdit", "Saving " + allTasks.size() + " tasks and updating instances");

        // Sačuvaj sve nove taskove (osim originalnog koji se samo update-uje)
        List<Task> tasksToSave = new ArrayList<>();
        for (Task task : allTasks) {
            if (!task.getId().equals(originalTaskId)) {
                tasksToSave.add(task);
            }
        }

        // Prvo sačuvaj nove taskove
        saveTasksSequentially(tasksToSave, 0, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Zatim update-uj originalni task (futureTask)
                if (futureTask != null) {
                    taskRepository.updateTask(futureTask, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void updateResult) {
                            // Na kraju update-uj instance i kreiraj DELETE instancu
                            updateInstancesAndCreateDeleteInstance(originalTaskId, allTasks, instances,
                                    pastTask, futureTask, instanceTasks, finalCallback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("TaskEdit", "Error updating future task", e);
                            finalCallback.onFailure(e);
                        }
                    });
                } else {
                    // Nema future task, samo update-uj instance
                    updateInstancesAndCreateDeleteInstance(originalTaskId, allTasks, instances,
                            pastTask, futureTask, instanceTasks, finalCallback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving new tasks", e);
                finalCallback.onFailure(e);
            }
        });
    }

    private void updateInstancesAndCreateDeleteInstance(String originalTaskId, List<Task> allTasks,
                                                        List<TaskInstance> instances, Task pastTask,
                                                        Task futureTask, List<Task> instanceTasks,
                                                        RepositoryCallback<List<Task>> finalCallback) {

        //Kreira DELETE instance za sve datume gde imamo completed/canceled
        List<TaskInstance> deleteInstances = createDeleteInstancesForSpecialDates(originalTaskId, instances);

        if (deleteInstances.isEmpty()) {
            // Nema DELETE instanci za kreiranje, direktno nastavi
            updateExistingInstances(instances, allTasks, pastTask, futureTask, instanceTasks, 0, finalCallback);
        } else {
            // Sačuvaj DELETE instance sekvencijalno
            saveDeleteInstances(deleteInstances, 0, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("TaskEdit", "All DELETE instances created");

                    // Update postojećih instanci
                    updateExistingInstances(instances, allTasks, pastTask, futureTask, instanceTasks, 0, finalCallback);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("TaskEdit", "Error creating DELETE instances", e);
                    finalCallback.onFailure(e);
                }
            });
        }
    }

    private List<TaskInstance> createDeleteInstancesForSpecialDates(String originalTaskId, List<TaskInstance> instances) {
        List<TaskInstance> deleteInstances = new ArrayList<>();
        Date today = getStartOfDay(new Date());

        if (instances != null) {
            String userId = !instances.isEmpty() ? instances.get(0).getUserId() : "";

            for (TaskInstance instance : instances) {
                if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                        instance.getNewStatus() == TaskStatus.CANCELED) {

                    Date instanceDate = getStartOfDay(instance.getOriginalDate());

                    // Kreira DELETE instancu samo za buduće datume
                    if (!instanceDate.before(today)) {
                        TaskInstance deleteInstance = new TaskInstance();
                        deleteInstance.setId(generateNewTaskId());
                        deleteInstance.setOriginalTaskId(originalTaskId);
                        deleteInstance.setUserId(userId);
                        deleteInstance.setOriginalDate(instance.getOriginalDate());
                        deleteInstance.setNewStatus(TaskStatus.DELETED);
                        deleteInstance.setCompletionDate(instance.getOriginalDate());

                        deleteInstances.add(deleteInstance);
                    }
                }
            }
        }

        return deleteInstances;
    }

    private void saveDeleteInstances(List<TaskInstance> deleteInstances, int currentIndex,
                                     RepositoryCallback<Void> callback) {
        if (currentIndex >= deleteInstances.size()) {
            callback.onSuccess(null);
            return;
        }

        TaskInstance deleteInstance = deleteInstances.get(currentIndex);
        taskInstanceRepository.addTaskInstance(deleteInstance, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("TaskEdit", "DELETE instance saved for date: " + deleteInstance.getOriginalDate());
                saveDeleteInstances(deleteInstances, currentIndex + 1, callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving DELETE instance", e);
                callback.onFailure(e);
            }
        });
    }

    private void updateExistingInstances(List<TaskInstance> instances, List<Task> allTasks,
                                         Task pastTask, Task futureTask, List<Task> instanceTasks,
                                         int currentIndex, RepositoryCallback<List<Task>> finalCallback) {

        if (instances == null || currentIndex >= instances.size()) {
            finalCallback.onSuccess(allTasks);
            return;
        }

        TaskInstance instance = instances.get(currentIndex);
        Date instanceDate = getStartOfDay(instance.getOriginalDate());

        Task targetTask = findTaskForInstance(instanceDate, instance.getNewStatus(),
                pastTask, futureTask, instanceTasks);

        if (targetTask != null) {
            instance.setOriginalTaskId(targetTask.getId());

            taskInstanceRepository.updateTaskInstance(instance, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    updateExistingInstances(instances, allTasks, pastTask, futureTask, instanceTasks,
                            currentIndex + 1, finalCallback);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("TaskEdit", "Error creating DELETE instances", e);
                    finalCallback.onFailure(e);
                }
            });
        } else {
            updateExistingInstances(instances, allTasks, pastTask, futureTask, instanceTasks,
                    currentIndex + 1, finalCallback);
        }
    }

    private Task findTaskForInstance(Date instanceDate, TaskStatus instanceStatus,
                                     Task pastTask, Task futureTask, List<Task> instanceTasks) {

        // Prvo proveri da li postoji specifičan task za completed/canceled instance
        if (instanceStatus == TaskStatus.COMPLETED || instanceStatus == TaskStatus.CANCELED) {
            for (Task instanceTask : instanceTasks) {
                if (dateInRange(instanceDate, instanceTask.getRecurrenceStartDate(),
                        instanceTask.getRecurrenceEndDate())) {
                    return instanceTask;
                }
            }
        }

        // Zatim proveri past task
        if (pastTask != null &&
                dateInRange(instanceDate, pastTask.getRecurrenceStartDate(), pastTask.getRecurrenceEndDate()) &&
                isValidRecurrenceDate(pastTask, instanceDate)) {
            return pastTask;
        }

        // Na kraju proveri future task
        if (futureTask != null &&
                dateInRange(instanceDate, futureTask.getRecurrenceStartDate(), futureTask.getRecurrenceEndDate()) &&
                isValidRecurrenceDate(futureTask, instanceDate)) {
            return futureTask;
        }

        return null;
    }

    // Helper methods (ostaju isti kao u originalnom kodu)

    private boolean dateInRange(Date date, Date startDate, Date endDate) {
        Date normalizedDate = getStartOfDay(date);
        Date normalizedStart = getStartOfDay(startDate);
        Date normalizedEnd = getStartOfDay(endDate);

        return !normalizedDate.before(normalizedStart) && !normalizedDate.after(normalizedEnd);
    }

    private void saveTasksSequentially(List<Task> tasksToSave, int currentIndex,
                                       RepositoryCallback<Void> callback) {
        if (currentIndex >= tasksToSave.size()) {
            callback.onSuccess(null);
            return;
        }

        Task currentTask = tasksToSave.get(currentIndex);
        taskRepository.addTask(currentTask, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                saveTasksSequentially(tasksToSave, currentIndex + 1, callback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving task", e);
                callback.onFailure(e);
            }
        });
    }

    private boolean canEditTask(Task originalTask, Date instanceDate) {
        if (!originalTask.isRecurring()) {
            return originalTask.getStatus() != TaskStatus.DELETED &&
                    originalTask.getStatus() != TaskStatus.COMPLETED &&
                    originalTask.getStatus() != TaskStatus.CANCELED;
        }
        return true;
    }

    private void editSingleTask(Task originalTask, Task editedTask, RepositoryCallback<List<Task>> finalCallback) {
        applyEditsToTask(originalTask, editedTask);

        taskRepository.updateTask(originalTask, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Task> resultTasks = new ArrayList<>();
                resultTasks.add(originalTask);
                finalCallback.onSuccess(resultTasks);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving new tasks", e);
                finalCallback.onFailure(e);
            }
        });
    }

    private void applyEditsToTask(Task target, Task source) {
        target.setTitle(source.getTitle());
        target.setDescription(source.getDescription());
        target.setDifficulty(source.getDifficulty());
        target.setImportance(source.getImportance());
        target.setExecutionTime(source.getExecutionTime());
    }

    private Task cloneTask(Task original) {
        Task clone = new Task();
        clone.setId(generateNewTaskId());
        clone.setTitle(original.getTitle());
        clone.setDescription(original.getDescription());
        clone.setCategoryId(original.getCategoryId());
        clone.setDifficulty(original.getDifficulty());
        clone.setImportance(original.getImportance());
        clone.setStatus(original.getStatus());
        clone.setUserId(original.getUserId());
        clone.setExecutionTime(original.getExecutionTime());
        clone.setRecurring(original.isRecurring());
        clone.setRecurrenceInterval(original.getRecurrenceInterval());
        clone.setRecurrenceUnit(original.getRecurrenceUnit());
        clone.setRecurrenceStartDate(original.getRecurrenceStartDate());
        clone.setRecurrenceEndDate(original.getRecurrenceEndDate());

        return clone;
    }

    private String generateNewTaskId() {
        return UUID.randomUUID().toString();
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

    private Date getStartOfDay(Date date) {
        if (date == null) return null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Date getPreviousDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return getStartOfDay(calendar.getTime());
    }

    private Date findNextValidRecurrenceDate(Task task, Date fromDate) {
        Date startDate = getStartOfDay(task.getRecurrenceStartDate());
        Date endDate = getStartOfDay(task.getRecurrenceEndDate());
        Date searchFrom = getStartOfDay(fromDate);

        if (searchFrom.before(startDate)) {
            return startDate;
        }

        if (searchFrom.after(endDate)) {
            return null;
        }

        long diffInMillis = searchFrom.getTime() - startDate.getTime();
        int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));

        int interval = task.getRecurrenceInterval();
        RecurrenceUnit unit = task.getRecurrenceUnit();

        if (unit == RecurrenceUnit.DAY) {
            int remainder = diffInDays % interval;
            if (remainder == 0) {
                return searchFrom;
            } else {
                int daysToAdd = interval - remainder;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTime(searchFrom);
                resultCal.add(Calendar.DAY_OF_MONTH, daysToAdd);
                Date result = getStartOfDay(resultCal.getTime());
                return result.after(endDate) ? null : result;
            }
        } else if (unit == RecurrenceUnit.WEEK) {
            int diffInWeeks = diffInDays / 7;
            int remainder = diffInWeeks % interval;

            if (remainder == 0 && (diffInDays % 7 == 0)) {
                return searchFrom;
            } else {
                int weeksToAdd = (remainder == 0) ? interval : (interval - remainder);
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTime(startDate);
                resultCal.add(Calendar.WEEK_OF_YEAR, diffInWeeks + weeksToAdd);
                Date result = getStartOfDay(resultCal.getTime());
                return result.after(endDate) ? null : result;
            }
        }

        return searchFrom;
    }

    private boolean isValidRecurrenceDate(Task task, Date date) {
        Date startDate = getStartOfDay(task.getRecurrenceStartDate());
        long diffInMillis = date.getTime() - startDate.getTime();
        int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));

        if (diffInDays < 0) {
            return false;
        }

        int interval = task.getRecurrenceInterval();
        RecurrenceUnit unit = task.getRecurrenceUnit();

        if (unit == RecurrenceUnit.DAY) {
            return diffInDays % interval == 0;
        } else if (unit == RecurrenceUnit.WEEK) {
            return (diffInDays % 7 == 0) && ((diffInDays / 7) % interval == 0);
        }

        return false;
    }
}