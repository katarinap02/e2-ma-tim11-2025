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
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

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

        // Inicijalizuj groupId ako ne postoji
        if (originalTask.getGroupId() == null) {
            originalTask.setGroupId(originalTask.getId());
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
            // Za recurring zadatke, radi sa celom grupom
            editTaskGroup(originalTask.getGroupId(), originalTask, editedTask, userId, instanceDate, finalCallback);
        }
    }

    private void editTaskGroup(String groupId, Task triggerTask, Task editedTask, String userId,
                               Date instanceDate, RepositoryCallback<List<Task>> finalCallback) {

        Log.d("TaskEdit", "Editing task group: " + groupId);

        // Prvo učitaj sve zadatke u grupi
        taskRepository.getTasksByGroupId(groupId, userId, new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasksInGroup) {
                if (tasksInGroup.isEmpty()) {
                    // Nema drugih zadataka u grupi, tretaj kao individualni
                    editRecurringTask(triggerTask, editedTask, userId, instanceDate, finalCallback);
                    return;
                }

                Log.d("TaskEdit", "Found " + tasksInGroup.size() + " tasks in group");

                // Ako je grupa već podeljena (više od 1 zadatka), proveravaj instance
                if (tasksInGroup.size() > 1) {
                    updateTasksInGroupWithInstanceCheck(tasksInGroup, editedTask, userId, finalCallback);
                } else {
                    // Prvi put se deli, nastavi sa normalnom logikom
                    editRecurringTask(triggerTask, editedTask, userId, instanceDate, finalCallback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error loading task group", e);
                // Fallback na individualnu izmenu
                editRecurringTask(triggerTask, editedTask, userId, instanceDate, finalCallback);
            }
        });
    }

    private void updateTasksInGroupWithInstanceCheck(List<Task> tasksInGroup, Task editedTask, String userId,
                                                     RepositoryCallback<List<Task>> finalCallback) {

        Log.d("TaskEdit", "Checking instances for " + tasksInGroup.size() + " tasks in group");

        // Rezultujuća lista zadataka koje treba vratiti
        List<Task> resultTasks = new ArrayList<>();

        // Pozovi asinhronu metodu za proveru i ažuriranje
        checkTaskInstancesAndUpdate(tasksInGroup, editedTask, userId, 0, resultTasks, finalCallback);
    }

    private void checkTaskInstancesAndUpdate(List<Task> tasksInGroup, Task editedTask, String userId,
                                             int currentIndex, List<Task> resultTasks,
                                             RepositoryCallback<List<Task>> finalCallback) {

        if (currentIndex >= tasksInGroup.size()) {
            // Završili smo sa svim zadacima
            Log.d("TaskEdit", "Finished checking all tasks. Result: " + resultTasks.size() + " tasks");
            finalCallback.onSuccess(resultTasks);
            return;
        }

        Task currentTask = tasksInGroup.get(currentIndex);
        Log.d("TaskEdit", "Checking task " + (currentIndex + 1) + "/" + tasksInGroup.size() +
                ": " + currentTask.getId());

        // Proveri da li ovaj zadatak ima instance
        taskInstanceRepository.getTaskInstancesForTask(userId, currentTask.getId(), new RepositoryCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> instances) {
                boolean hasInstances = instances != null && !instances.isEmpty();
                Log.d("TaskEdit", "Task " + currentTask.getId() + " has " +
                        (hasInstances ? instances.size() : 0) + " instances");

                if (hasInstances) {
                    // Zadatak ima instance - ostavi ga nepromenjenog
                    Log.d("TaskEdit", "Task " + currentTask.getId() + " has instances, keeping original");
                    resultTasks.add(currentTask);
                    // Nastavi sa sledećim zadatkom
                    checkTaskInstancesAndUpdate(tasksInGroup, editedTask, userId,
                            currentIndex + 1, resultTasks, finalCallback);
                } else {
                    // Zadatak nema instance - primeni izmene
                    Log.d("TaskEdit", "Task " + currentTask.getId() + " has no instances, applying edits");
                    applyEditsToTask(currentTask, editedTask);

                    // Sačuvaj izmenjeni zadatak
                    taskRepository.updateTask(currentTask, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            resultTasks.add(currentTask);
                            // Nastavi sa sledećim zadatkom
                            checkTaskInstancesAndUpdate(tasksInGroup, editedTask, userId,
                                    currentIndex + 1, resultTasks, finalCallback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("TaskEdit", "Error updating task " + currentTask.getId(), e);
                            finalCallback.onFailure(e);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error checking instances for task " + currentTask.getId(), e);
                // U slučaju greške, tretaj kao da nema instance i primeni izmene
                applyEditsToTask(currentTask, editedTask);

                taskRepository.updateTask(currentTask, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        resultTasks.add(currentTask);
                        checkTaskInstancesAndUpdate(tasksInGroup, editedTask, userId,
                                currentIndex + 1, resultTasks, finalCallback);
                    }

                    @Override
                    public void onFailure(Exception updateError) {
                        Log.e("TaskEdit", "Error updating task after instance check failed", updateError);
                        finalCallback.onFailure(updateError);
                    }
                });
            }
        });
    }

    private void editRecurringTask(Task originalTask, Task editedTask, String userId,
                                   Date instanceDate, RepositoryCallback<List<Task>> finalCallback) {

        Log.d("TaskEdit", "Editing recurring task: " + originalTask.getId() + " for date: " + instanceDate);

        // Prvo učitaj postojeće instance
        taskInstanceRepository.getTaskInstancesForTask(userId, originalTask.getId(), new RepositoryCallback<List<TaskInstance>>() {
            @Override
            public void onSuccess(List<TaskInstance> instances) {
                Log.d("TaskEdit", "Got " + instances.size() + " existing instances");

                // Proveri da li se pokušava menjati instanca koja je completed ili canceled
                for (TaskInstance instance : instances) {
                    if (isSameDay(instance.getOriginalDate(), instanceDate)) {
                        if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                                instance.getNewStatus() == TaskStatus.CANCELED) {
                            finalCallback.onFailure(new Exception("Ne možete menjati zadatak koji je već završen ili otkazan za taj datum."));
                            return;
                        }
                    }
                }

                // Generiši nove zadatke na osnovu izmene
                List<Task> newTasks = generateTasksFromEdit(originalTask, editedTask, instanceDate, instances);

                if (newTasks.isEmpty()) {
                    finalCallback.onFailure(new Exception("Greška pri generisanju novih zadataka."));
                    return;
                }

                // Sačuvaj nove zadatke u bazu
                saveNewTasks(originalTask.getId(), originalTask.getUserId(), newTasks, instances, finalCallback);

            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error loading task instances", e);
                finalCallback.onFailure(e);
            }
        });
    }

    private List<Task> generateTasksFromEdit(Task originalTask, Task editedTask, Date instanceDate,
                                             List<TaskInstance> instances) {

        List<Task> resultTasks = new ArrayList<>();
        Date today = getStartOfDay(new Date());
        Date startDate = getStartOfDay(originalTask.getRecurrenceStartDate());
        Date endDate = getStartOfDay(originalTask.getRecurrenceEndDate());

        // Napravi listu "zaštićenih" datuma - oni koji imaju completed/cancelled instance
        List<Date> protectedDates = new ArrayList<>();
        if (instances != null) {
            for (TaskInstance instance : instances) {
                if (instance.getNewStatus() == TaskStatus.COMPLETED ||
                        instance.getNewStatus() == TaskStatus.CANCELED) {
                    protectedDates.add(getStartOfDay(instance.getOriginalDate()));
                }
            }
            protectedDates.sort(Date::compareTo);
        }

        Log.d("TaskEdit", "Today: " + today + ", Start: " + startDate + ", End: " + endDate);
        Log.d("TaskEdit", "Protected dates: " + protectedDates.size());

        // ZADATAK 1: Prošlost - sve do danas (uključujući danas ako je u prošlosti)
        if (startDate.before(today)) {
            Date pastEnd = getPreviousDay(today); // Do juče

            if (!startDate.after(pastEnd)) {
                Task pastTask = cloneTask(originalTask);
                pastTask.setRecurrenceStartDate(startDate);
                pastTask.setRecurrenceEndDate(pastEnd);
                resultTasks.add(pastTask);
                Log.d("TaskEdit", "Created past task from " + startDate + " to " + pastEnd);
            }
        }

        // ZADATAK 2: Pronadji prvi validan datum
        Date futureStart = findNextValidRecurrenceDate(originalTask, today);

        if (futureStart != null && !futureStart.after(endDate)) {
            if (protectedDates.isEmpty()) {
                // Nema zaštićenih datuma - jedan veliki izmenjeni period
                Task futureTask = cloneTask(originalTask);
                applyEditsToTask(futureTask, editedTask);
                futureTask.setRecurrenceStartDate(futureStart);
                futureTask.setRecurrenceEndDate(endDate);
                resultTasks.add(futureTask);
                Log.d("TaskEdit", "Created single future task from " + futureStart + " to " + endDate);
            } else {
                // Ima zaštićenih datuma - treba kreirati više segmenata
                createFutureSegments(originalTask, editedTask, futureStart, endDate, protectedDates, resultTasks);
            }
        }

        return resultTasks;
    }

    private void createFutureSegments(Task originalTask, Task editedTask, Date futureStart, Date endDate,
                                      List<Date> protectedDates, List<Task> resultTasks) {

        Date currentStart = futureStart;

        for (Date protectedDate : protectedDates) {
            // Preskoči zaštićene datume koji su pre našeg početka
            if (protectedDate.before(futureStart)) {
                continue;
            }

            // Preskoči zaštićene datume koji su posle našeg kraja
            if (protectedDate.after(endDate)) {
                break;
            }

            // Kreiraj izmenjeni segment PRE zaštićenog datuma
            Date segmentEnd = getPreviousDay(protectedDate);

            if (!currentStart.after(segmentEnd)) {
                Task editedSegment = cloneTask(originalTask);
                applyEditsToTask(editedSegment, editedTask);
                editedSegment.setRecurrenceStartDate(currentStart);
                editedSegment.setRecurrenceEndDate(segmentEnd);
                resultTasks.add(editedSegment);
                Log.d("TaskEdit", "Created edited segment from " + currentStart + " to " + segmentEnd);
            }

            // Kreiraj originalni segment ZA zaštićeni datum (samo taj jedan dan)
            Task protectedSegment = cloneTask(originalTask);
            protectedSegment.setRecurrenceStartDate(protectedDate);
            protectedSegment.setRecurrenceEndDate(protectedDate);
            resultTasks.add(protectedSegment);
            Log.d("TaskEdit", "Created protected segment for " + protectedDate);

            // Pomeri start za sledeći segment
            currentStart = findNextValidRecurrenceDate(originalTask, getNextDay(protectedDate));
        }

        // Kreiraj poslednji izmenjeni segment POSLE poslednjeg zaštićenog datuma
        if (currentStart != null && !currentStart.after(endDate)) {
            Date validStart = findNextValidRecurrenceDate(originalTask, currentStart);

            if (validStart != null && !validStart.after(endDate)) {
                Task finalSegment = cloneTask(originalTask);
                applyEditsToTask(finalSegment, editedTask);
                finalSegment.setRecurrenceStartDate(validStart);
                finalSegment.setRecurrenceEndDate(endDate);
                resultTasks.add(finalSegment);
                Log.d("TaskEdit", "Created final edited segment from " + validStart + " to " + endDate);
            }
        }
    }

    private void saveNewTasks(String originalTaskId, String userId, List<Task> newTasks, List<TaskInstance> instances,
                              RepositoryCallback<List<Task>> finalCallback) {
        Log.d("TaskEdit", "Saving " + newTasks.size() + " new tasks and updating " +
                (instances != null ? instances.size() : 0) + " instances");

        // Prvo sačuvaj nove zadatke
        saveTasksSequentially(newTasks, 0, new ArrayList<>(), new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> savedTasks) {
                Log.d("TaskEdit", "All tasks saved, now updating instances");

                // Zatim prebaci instance na odgovarajuće nove zadatke
                updateTaskInstances(originalTaskId, savedTasks, instances, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        // Na kraju obriši originalni zadatak
                        taskRepository.deleteTaskFromDatabase(originalTaskId, userId, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void deleteResult) {
                                Log.d("TaskEdit", "Original task deleted: " + originalTaskId);
                                finalCallback.onSuccess(savedTasks);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("TaskEdit", "Error deleting original task", e);
                                finalCallback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        Log.e("TaskEdit", "Error updating instances", e);
                        finalCallback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving new tasks", e);
                finalCallback.onFailure(e);
            }
        });
    }

    private void updateTaskInstances(String originalTaskId, List<Task> newTasks, List<TaskInstance> instances,
                                     RepositoryCallback<Void> callback) {
        if (instances == null || instances.isEmpty()) {
            Log.d("TaskEdit", "No instances to update");
            callback.onSuccess(null);
            return;
        }

        Log.d("TaskEdit", "Updating " + instances.size() + " instances across " + newTasks.size() + " new tasks");

        // Kreiraj mapu za lakše pronalaženje task-ova
        Map<String, Task> taskMap = new HashMap<>();
        for (Task task : newTasks) {
            taskMap.put(task.getId(), task);
            Log.d("TaskEdit", "Task " + task.getId() + " covers: " +
                    task.getRecurrenceStartDate() + " to " + task.getRecurrenceEndDate());
        }

        updateInstancesSequentiallyImproved(instances, taskMap, 0, callback);
    }

    private void updateInstancesSequentiallyImproved(List<TaskInstance> instances, Map<String, Task> taskMap,
                                                     int currentIndex, RepositoryCallback<Void> callback) {
        if (currentIndex >= instances.size()) {
            Log.d("TaskEdit", "All instances updated successfully");
            callback.onSuccess(null);
            return;
        }

        TaskInstance instance = instances.get(currentIndex);
        Date instanceDate = getStartOfDay(instance.getOriginalDate());

        // Pronađi odgovarajući task sa poboljšanom logikom
        Task targetTask = findTaskForDateImproved(taskMap.values(), instanceDate);

        if (targetTask != null) {
            Log.d("TaskEdit", "Updating instance " + instanceDate + " from task " +
                    instance.getOriginalTaskId() + " to task " + targetTask.getId());

            // Kreiraj novu instancu sa ažuriranim originalTaskId
            TaskInstance updatedInstance = new TaskInstance();
            updatedInstance.setId(instance.getId());
            updatedInstance.setOriginalTaskId(targetTask.getId()); // Ovo je ključna promena
            updatedInstance.setUserId(instance.getUserId());
            updatedInstance.setOriginalDate(instance.getOriginalDate());
            updatedInstance.setNewStatus(instance.getNewStatus());
            updatedInstance.setCompletionDate(instance.getCompletionDate());

            taskInstanceRepository.updateTaskInstance(updatedInstance, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    updateInstancesSequentiallyImproved(instances, taskMap, currentIndex + 1, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("TaskEdit", "Error updating instance at index " + currentIndex +
                            " for date " + instanceDate, e);
                    callback.onFailure(e);
                }
            });
        } else {
            Log.w("TaskEdit", "No target task found for instance date: " + instanceDate +
                    ". Available tasks:");
            for (Task task : taskMap.values()) {
                Log.w("TaskEdit", "  Task " + task.getId() + ": " +
                        task.getRecurrenceStartDate() + " to " + task.getRecurrenceEndDate());
            }

            // Preskoči ovu instancu i nastavi sa sledećom
            updateInstancesSequentiallyImproved(instances, taskMap, currentIndex + 1, callback);
        }
    }

    private Task findTaskForDateImproved(Collection<Task> tasks, Date date) {
        Log.d("TaskEdit", "Looking for task for date: " + date);

        for (Task task : tasks) {
            Date taskStart = getStartOfDay(task.getRecurrenceStartDate());
            Date taskEnd = getStartOfDay(task.getRecurrenceEndDate());

            Log.d("TaskEdit", "Checking task " + task.getId() +
                    " (" + taskStart + " to " + taskEnd + ")");

            // Proveri da li datum spada u opseg ovog task-a
            if (!date.before(taskStart) && !date.after(taskEnd)) {
                Log.d("TaskEdit", "Date is in range, checking recurrence pattern");

                // Proveri da li datum spada u recurrence pattern ovog zadatka
                if (isValidRecurrenceDate(task, date)) {
                    Log.d("TaskEdit", "Found matching task: " + task.getId());
                    return task;
                } else {
                    Log.d("TaskEdit", "Date not valid for recurrence pattern");
                }
            } else {
                Log.d("TaskEdit", "Date not in task range");
            }
        }

        Log.w("TaskEdit", "No matching task found for date: " + date);
        return null;
    }

    private boolean isValidRecurrenceDate(Task task, Date date) {
        Date startDate = getStartOfDay(task.getRecurrenceStartDate());
        long diffInMillis = date.getTime() - startDate.getTime();
        int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));

        if (diffInDays < 0) {
            Log.d("TaskEdit", "Date is before task start date");
            return false;
        }

        int interval = task.getRecurrenceInterval();
        RecurrenceUnit unit = task.getRecurrenceUnit();

        Log.d("TaskEdit", "Checking recurrence: diffInDays=" + diffInDays +
                ", interval=" + interval + ", unit=" + unit);

        if (unit == RecurrenceUnit.DAY) {
            boolean isValid = diffInDays % interval == 0;
            Log.d("TaskEdit", "Daily recurrence check: " + isValid);
            return isValid;
        } else if (unit == RecurrenceUnit.WEEK) {
            boolean isValid = (diffInDays % 7 == 0) && ((diffInDays / 7) % interval == 0);
            Log.d("TaskEdit", "Weekly recurrence check: " + isValid +
                    " (diffInDays % 7 = " + (diffInDays % 7) +
                    ", (diffInDays / 7) % interval = " + ((diffInDays / 7) % interval) + ")");
            return isValid;
        }

        Log.w("TaskEdit", "Unknown recurrence unit: " + unit);
        return false;
    }

    private void saveTasksSequentially(List<Task> tasksToSave, int currentIndex, List<Task> savedTasks,
                                       RepositoryCallback<List<Task>> finalCallback) {

        if (currentIndex >= tasksToSave.size()) {
            // Svi zadaci su sačuvani
            Log.d("TaskEdit", "Svi zadaci su uspesno sacuvani: " + savedTasks.size());
            finalCallback.onSuccess(savedTasks);
            return;
        }

        Task currentTask = tasksToSave.get(currentIndex);
        Log.d("TaskEdit", "Saving task " + (currentIndex + 1) + "/" + tasksToSave.size());

        taskRepository.addTask(currentTask, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                savedTasks.add(currentTask);
                saveTasksSequentially(tasksToSave, currentIndex + 1, savedTasks, finalCallback);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("TaskEdit", "Error saving task at index " + currentIndex, e);
                finalCallback.onFailure(e);
            }
        });
    }

    private boolean canEditTask(Task originalTask, Date instanceDate) {
        if (!originalTask.isRecurring()) {
            return originalTask.getStatus() != TaskStatus.DELETED &&
                    originalTask.getStatus() != TaskStatus.COMPLETED &&
                    originalTask.getStatus() != TaskStatus.CANCELED &&
                    originalTask.getStatus() != TaskStatus.UNCOMPLETED;
        }
        return true;
    }

    private void editSingleTask(Task originalTask, Task editedTask, RepositoryCallback<List<Task>> finalCallback) {

        // Primeni izmene na originalni zadatak
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

        // KLJUČNO: Svi segmenti imaju isti groupId
        clone.setGroupId(original.getGroupId() != null ? original.getGroupId() : original.getId());

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

    private Date getNextDay(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        return getStartOfDay(calendar.getTime());
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

        // Ako je searchFrom pre početka zadatka, vrati start date
        if (searchFrom.before(startDate)) {
            return startDate;
        }

        // Ako je searchFrom posle kraja zadatka, nema validnih datuma
        if (searchFrom.after(endDate)) {
            return null;
        }

        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startDate);

        Calendar searchCal = Calendar.getInstance();
        searchCal.setTime(searchFrom);

        // Izračunaj broj dana od početka do search datuma
        long diffInMillis = searchFrom.getTime() - startDate.getTime();
        int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));

        int interval = task.getRecurrenceInterval();
        RecurrenceUnit unit = task.getRecurrenceUnit();

        if (unit == RecurrenceUnit.DAY) {
            // Za dnevne zadatke
            int remainder = diffInDays % interval;

            if (remainder == 0) {
                // searchFrom je tačno na recurrence datumu
                return searchFrom;
            } else {
                // Pronađi sledeći validan datum
                int daysToAdd = interval - remainder;
                Calendar resultCal = Calendar.getInstance();
                resultCal.setTime(searchFrom);
                resultCal.add(Calendar.DAY_OF_MONTH, daysToAdd);

                Date result = getStartOfDay(resultCal.getTime());
                return result.after(endDate) ? null : result;
            }
        } else if (unit == RecurrenceUnit.WEEK) {
            // Za nedeljne zadatke
            int diffInWeeks = diffInDays / 7;
            int remainder = diffInWeeks % interval;

            if (remainder == 0 && (diffInDays % 7 == 0)) {
                // searchFrom je tačno na recurrence datumu
                return searchFrom;
            } else {
                // Pronađi sledeći validan datum
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
}