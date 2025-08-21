package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.CategoryRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.usecase.TaskUseCase;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends ViewModel{
    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;

    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public final LiveData<List<Task>> tasks = _tasks;
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    public final LiveData<List<Category>> categories = _categories;

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

    public TaskViewModel(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
    }

    public void loadTasks(String userId) {
        taskRepository.getTasks(userId, new RepositoryCallback<List<Task>>() { // PROVERI DA LI JE getTasksForUser
            @Override
            public void onSuccess(List<Task> result) {
                _tasks.postValue(result);
                _isLoading.postValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
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

    public void completeTask(Task task, String userId)
    {
        _isSaving.setValue(true);

        TaskUseCase taskUseCase = new TaskUseCase(taskRepository);
        taskUseCase.completeTask(task, userId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer earnedXp) {
                // TODO: Ažuriraj LiveData koji prikazuje poruku o uspehu
                // npr. _successMessage.postValue("Zadatak završen! + " + earnedXp + " XP");

                // Ponovo učitaj zadatke da se vidi promena statusa
                loadTasks(userId);
                _isSaving.postValue(false);
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

                loadTasks(userId);
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
        public Factory(Application application) {
            this.application = application;
        }
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(TaskViewModel.class)) {
                try {
                    TaskRepository taskRepo = new TaskRepositoryImpl(application);
                    CategoryRepository catRepo = new CategoryRepositoryImpl(application);
                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(TaskRepository.class, CategoryRepository.class)
                            .newInstance(taskRepo, catRepo);
                    return viewModel;

                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
