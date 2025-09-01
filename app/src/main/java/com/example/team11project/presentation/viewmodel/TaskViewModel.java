package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.CategoryRepositoryImpl;
import com.example.team11project.data.repository.LevelInfoRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.model.TaskInstance;
import com.example.team11project.domain.model.TaskStatus;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.usecase.TaskEditUseCase;
import com.example.team11project.domain.usecase.TaskUseCase;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskViewModel extends ViewModel{
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    private final TaskUseCase taskUseCase;

    private final TaskInstanceRepository taskInstanceRepository;

    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public final LiveData<List<Task>> tasks = _tasks;
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    public final LiveData<List<Category>> categories = _categories;

    private final MutableLiveData<Task> _selectedTask = new MutableLiveData<>();
    public final LiveData<Task> selectedTask = _selectedTask;

    private final MutableLiveData<Category> _selectedTaskCategory = new MutableLiveData<>();
    public final LiveData<Category> selectedTaskCategory = _selectedTaskCategory;

    // Za sve instance (izuzetke) vezane za taj zadatak
    private final MutableLiveData<List<TaskInstance>> _selectedTaskInstances = new MutableLiveData<>();
    public final LiveData<List<TaskInstance>> selectedTaskInstances = _selectedTaskInstances;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _errorCategory = new MutableLiveData<>();
    public final LiveData<String> errorCategory = _errorCategory;
    private final MutableLiveData<Boolean> _isLoadingCategory = new MutableLiveData<>();
    public final LiveData<Boolean> isLoadingCategory = _isLoadingCategory;

    // za cuvanje malo bolji nacin
    private final MutableLiveData<Boolean> _taskSaveSuccess = new MutableLiveData<>(false);
    public final LiveData<Boolean> taskSaveSuccess = _taskSaveSuccess;

    private final MutableLiveData<Boolean> _isSaving = new MutableLiveData<>(false);
    public final LiveData<Boolean> isSaving = _isSaving;

    //poruka koliko je xp osvojio
    private final MutableLiveData<Integer> _taskCompletedXp = new MutableLiveData<>();
    public final LiveData<Integer> taskCompletedXp = _taskCompletedXp;

    // --- NOVI LIVE DATA ZA MAPE INSTANCI ---
    private final MutableLiveData<Map<String, List<TaskInstance>>> _instancesMap = new MutableLiveData<>();
    public final LiveData<Map<String, List<TaskInstance>>> instancesMap = _instancesMap;

    private final MutableLiveData<Boolean> _statusChangeCompleted = new MutableLiveData<>();
    public final LiveData<Boolean> statusChangeCompleted = _statusChangeCompleted;

    private final MutableLiveData<Boolean> _taskCompleteSuccess = new MutableLiveData<>();
    public final LiveData<Boolean> taskCompleteSuccess = _taskCompleteSuccess;

    public TaskViewModel(TaskRepository taskRepository, CategoryRepository categoryRepository, TaskUseCase taskUseCase, TaskInstanceRepository taskInstanceRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.taskUseCase = taskUseCase;
        this.taskInstanceRepository = taskInstanceRepository;
    }

    public void loadTasksAndInstances(String userId) {
        _isLoading.postValue(true);
        taskRepository.getTasks(userId, new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                _tasks.postValue(result);
                fetchInstancesForTasks(result, userId);
            }
            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    // Pomoćna metoda za povlačenje instanci
    private void fetchInstancesForTasks(List<Task> tasks, String userId) {
        Map<String, List<TaskInstance>> finalMap = new HashMap<>();
        AtomicInteger tasksProcessed = new AtomicInteger(0);
        int recurringTasksCount = 0;

        // Izbroj koliko ponavljajućih zadataka uopšte imamo
        for (Task t : tasks) if (t.isRecurring()) recurringTasksCount++;

        if (recurringTasksCount == 0) {
            // Ako nema ponavljajućih, odmah završi i ugasi loading
            _instancesMap.postValue(finalMap); // Pošalji praznu mapu
            _isLoading.postValue(false);
            return;
        }

        // Za svaki ponavljajući zadatak, dohvati njegove instance
        for (Task task : tasks) {
            if (task.isRecurring()) {
                int finalRecurringTasksCount = recurringTasksCount;
                taskInstanceRepository.getTaskInstancesForTask(userId, task.getId(), new RepositoryCallback<List<TaskInstance>>() {
                    @Override
                    public void onSuccess(List<TaskInstance> instances) {
                        finalMap.put(task.getId(), instances);
                        // Proveri da li smo završili sa svim zadacima
                        if (tasksProcessed.incrementAndGet() == finalRecurringTasksCount) {
                            _instancesMap.postValue(finalMap);
                            _isLoading.postValue(false);
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        if (tasksProcessed.incrementAndGet() == finalRecurringTasksCount) {
                            _instancesMap.postValue(finalMap);
                            _isLoading.postValue(false);
                        }
                    }
                });
            }
        }
    }

    public void loadCategories(String userId) {
        _isLoadingCategory.setValue(true);
        categoryRepository.getCategories(userId, new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                _categories.postValue(result);
                _isLoadingCategory.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _errorCategory.postValue(e.getMessage());
                _isLoadingCategory.postValue(false);
            }
        });
    }

    public void createNewTask(Task task)
    {
        _isSaving.setValue(true);
        taskRepository.addTask(task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isSaving.postValue(false);
                _taskSaveSuccess.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isSaving.postValue(false);
            }
        });
    }

    public void loadTaskDetails(String taskId, String userId) {
        _isLoading.setValue(true);
        taskRepository.getTaskById(taskId, userId, new RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task task) {
                _selectedTask.postValue(task);
                // Trazimo kategoriju posle zadatka
                if (task.getCategoryId() != null) {
                    categoryRepository.getCategoryById(task.getCategoryId(), userId, new RepositoryCallback<Category>() {
                        @Override
                        public void onSuccess(Category category) {
                            _selectedTaskCategory.postValue(category);
                            // sada dobavljamo i instance posle kategorije
                            if (task.isRecurring()) {
                                taskInstanceRepository.getTaskInstancesForTask(userId, taskId, new RepositoryCallback<List<TaskInstance>>() {
                                    @Override public void onSuccess(List<TaskInstance> instances) {
                                        _selectedTaskInstances.postValue(instances);
                                        _isLoading.postValue(false);
                                    }
                                    @Override public void onFailure(Exception e) {
                                        _selectedTaskInstances.postValue(new ArrayList<>()); // Pošalji praznu listu
                                        _isLoading.postValue(false);
                                    }
                                });
                            } else {
                                _selectedTaskInstances.postValue(new ArrayList<>()); // Jednokratni nema instance
                                _isLoading.postValue(false);
                            }
                        }
                        @Override
                        public void onFailure(Exception e) {
                            _selectedTaskCategory.postValue(null);
                            _isLoading.postValue(false);
                        }
                    });
                } else {
                    _selectedTaskCategory.postValue(null);
                    _isLoading.postValue(false);
                }
            }
            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    // Poziva UseCase za završavanje zadatka
    public void completeTask(Task task, String userId, Date instanceDate) {
        _isSaving.setValue(true);
        Task optimisticTask = new Task(task);
        optimisticTask.setStatus(TaskStatus.COMPLETED);
        optimisticTask.setCompletionDate(new Date());

        _selectedTask.postValue(optimisticTask);

        List<Task> currentTasks = _tasks.getValue();
        if (currentTasks != null) {
            List<Task> updatedTasksList = new ArrayList<>(currentTasks);
            for (int i = 0; i < updatedTasksList.size(); i++) {
                if (updatedTasksList.get(i).getId().equals(task.getId())) {
                    updatedTasksList.set(i, optimisticTask);
                    break;
                }
            }
            _tasks.postValue(updatedTasksList);
        }

        // 4. Asinhrono izvrši operaciju u bazi
        taskUseCase.completeTask(task, userId, instanceDate, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer earnedXp) {
                _isSaving.postValue(false);
                _taskCompletedXp.postValue(earnedXp);
                _taskCompleteSuccess.postValue(true);

                // Task je već ažuriran optimistički, samo potvrdi da je sve OK
                // Možeš dodati refresh instance-a ako je potrebno
                if (task.isRecurring()) {
                    taskInstanceRepository.getTaskInstancesForTask(userId, task.getId(), new RepositoryCallback<List<TaskInstance>>() {
                        @Override
                        public void onSuccess(List<TaskInstance> instances) {
                            _selectedTaskInstances.postValue(instances);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            // Ignoriši grešku za instance
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                _isSaving.postValue(false);
                _error.postValue(e.getMessage());
                _taskCompleteSuccess.postValue(false);

                // ROLLBACK: Vrati originalnu vrednost ako je operacija neuspešna
                _selectedTask.postValue(task);

                // Vrati i glavnu listu
                List<Task> currentTasks = _tasks.getValue();
                if (currentTasks != null) {
                    List<Task> revertedTasksList = new ArrayList<>(currentTasks);
                    for (int i = 0; i < revertedTasksList.size(); i++) {
                        if (revertedTasksList.get(i).getId().equals(task.getId())) {
                            revertedTasksList.set(i, task);
                            break;
                        }
                    }
                    _tasks.postValue(revertedTasksList);
                }
            }
        });
    }


    //promene statusa za taskove, uslove dodajemo kasnije
    public void changeTaskStatus(Task task, TaskStatus newStatus, String userId, Date instanceDate) {
        // 1. ODMAH ažuriraj UI optimistički
        Task optimisticTask = new Task(task);
        optimisticTask.setStatus(newStatus);
        _selectedTask.postValue(optimisticTask);

        // 2. Ažuriraj i glavnu listu zadataka
        List<Task> currentTasks = _tasks.getValue();
        if (currentTasks != null) {
            List<Task> updatedTasksList = new ArrayList<>(currentTasks);
            for (int i = 0; i < updatedTasksList.size(); i++) {
                if (updatedTasksList.get(i).getId().equals(task.getId())) {
                    updatedTasksList.set(i, optimisticTask);
                    break;
                }
            }
            _tasks.postValue(updatedTasksList);
        }

        // 3. Asinhrono pošalji promenu u bazu
        RepositoryCallback<Void> updateCallback = new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Potvrdi da je operacija uspešna
                _statusChangeCompleted.postValue(true);

                // Refresh instance-e ako je ponavljajući zadatak
                if (task.isRecurring()) {
                    taskInstanceRepository.getTaskInstancesForTask(userId, task.getId(), new RepositoryCallback<List<TaskInstance>>() {
                        @Override
                        public void onSuccess(List<TaskInstance> instances) {
                            _selectedTaskInstances.postValue(instances);
                        }
                        @Override
                        public void onFailure(Exception e) {
                            // Ignoriši grešku za instance
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                // ROLLBACK: Vrati originalnu vrednost ako je operacija neuspešna
                _selectedTask.postValue(task);

                // Vrati i glavnu listu
                List<Task> currentTasks = _tasks.getValue();
                if (currentTasks != null) {
                    List<Task> revertedTasksList = new ArrayList<>(currentTasks);
                    for (int i = 0; i < revertedTasksList.size(); i++) {
                        if (revertedTasksList.get(i).getId().equals(task.getId())) {
                            revertedTasksList.set(i, task);
                            break;
                        }
                    }
                    _tasks.postValue(revertedTasksList);
                }

                _error.postValue(e.getMessage());
                _statusChangeCompleted.postValue(false);
            }
        };

        // Izvršavanje operacije u bazi
        switch (newStatus) {
            case DELETED:
                taskRepository.deleteTask(task, updateCallback);
                break;
            case ACTIVE:
                taskRepository.activateTask(task, updateCallback);
                break;
            case PAUSED:
                taskRepository.pauseTask(task, updateCallback);
                break;
            case CANCELED:
                taskUseCase.cancelTask(task, userId, instanceDate, updateCallback);
                break;
        }
    }

    public void editTask(Task originalTask, Task editedTask, String userId, Date instanceDate) {
        _isSaving.setValue(true);

        // Koristi TaskEditUseCase koji već imaš
        TaskEditUseCase editUseCase = new TaskEditUseCase(taskRepository, taskInstanceRepository);

        editUseCase.editTask(originalTask, editedTask, userId, instanceDate, new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> updatedTasks) {
                _isSaving.postValue(false);
                _taskSaveSuccess.postValue(true);

                // Refresh task listu
                loadTasksAndInstances(userId);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isSaving.postValue(false);
            }
        });
    }

    public void loadInitialData(String userId) {
        _isLoading.setValue(true);
        _isLoadingCategory.setValue(true);

        categoryRepository.getCategories(userId, new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> categoryResult) {
                _categories.postValue(categoryResult);
                _isLoadingCategory.postValue(false);

                loadTasksAndInstances(userId);
            }
            @Override
            public void onFailure(Exception e) {
                _errorCategory.postValue(e.getMessage());
                _isLoadingCategory.postValue(false);
                _isLoading.postValue(false); // ugasi i drugi loading
            }
        });
    }

    //resetovanje signala
    public void onSaveSuccessNavigated() {
        _taskSaveSuccess.setValue(false);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        public Factory(Application application) { this.application = application; }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TaskViewModel.class)) {
                try {
                    TaskRepository taskRepo = new TaskRepositoryImpl(application);
                    LevelInfoRepository levelRepo = new LevelInfoRepositoryImpl(application);
                    CategoryRepository catRepo = new CategoryRepositoryImpl(application);
                    TaskInstanceRepository instanceRepo = new TaskInstanceRepositoryImpl(application);
                    TaskUseCase completeUC = new TaskUseCase(taskRepo, levelRepo, instanceRepo);


                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(TaskRepository.class, CategoryRepository.class, TaskUseCase.class, TaskInstanceRepository.class)
                            .newInstance(taskRepo, catRepo, completeUC, instanceRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
