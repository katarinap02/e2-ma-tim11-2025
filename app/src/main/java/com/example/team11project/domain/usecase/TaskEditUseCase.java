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
            // Kompleksna izmena ponavljajućeg zadatka
            editRecurringTask(originalTask, editedTask, userId, instanceDate, finalCallback);
        }
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

        if (!futureStart.after(endDate)) {
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
        if (currentStart != null &&!currentStart.after(endDate)) {
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
            callback.onSuccess(null);
            return;
        }

        updateInstancesSequentially(instances, newTasks, 0, callback);
    }

    private void updateInstancesSequentially(List<TaskInstance> instances, List<Task> newTasks,
                                             int currentIndex, RepositoryCallback<Void> callback) {
        if (currentIndex >= instances.size()) {
            Log.d("TaskEdit", "All instances updated successfully");
            callback.onSuccess(null);
            return;
        }

        TaskInstance instance = instances.get(currentIndex);
        Date instanceDate = getStartOfDay(instance.getOriginalDate());

        // Pronađi u koji novi zadatak spada ova instanca
        Task targetTask = findTaskForDate(newTasks, instanceDate);

        if (targetTask != null) {
            Log.d("TaskEdit", "Updating instance " + instanceDate + " to task " + targetTask.getId());

            // Ažuriraj originalTaskId instance
            instance.setOriginalTaskId(targetTask.getId());

            taskInstanceRepository.updateTaskInstance(instance, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    updateInstancesSequentially(instances, newTasks, currentIndex + 1, callback);
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("TaskEdit", "Error updating instance at index " + currentIndex, e);
                    callback.onFailure(e);
                }
            });
        } else {
            Log.w("TaskEdit", "No target task found for instance date: " + instanceDate);
            // Preskoči ovu instancu i nastavi sa sledećom
            updateInstancesSequentially(instances, newTasks, currentIndex + 1, callback);
        }
    }

    private Task findTaskForDate(List<Task> tasks, Date date) {
        for (Task task : tasks) {
            Date taskStart = getStartOfDay(task.getRecurrenceStartDate());
            Date taskEnd = getStartOfDay(task.getRecurrenceEndDate());

            if (!date.before(taskStart) && !date.after(taskEnd)) {
                // Proveri da li datum spada u recurrence pattern ovog zadatka
                if (isValidRecurrenceDate(task, date)) {
                    return task;
                }
            }
        }
        return null;
    }

    private boolean isValidRecurrenceDate(Task task, Date date) {
        Date startDate = getStartOfDay(task.getRecurrenceStartDate());
        long diffInMillis = date.getTime() - startDate.getTime();
        int diffInDays = (int) (diffInMillis / (24 * 60 * 60 * 1000));

        int interval = task.getRecurrenceInterval();
        RecurrenceUnit unit = task.getRecurrenceUnit();

        if (unit == RecurrenceUnit.DAY) {
            return diffInDays % interval == 0;
        } else if (unit == RecurrenceUnit.WEEK) {
            return (diffInDays % 7 == 0) && ((diffInDays / 7) % interval == 0);
        }

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
        //searchFrom je danas
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
