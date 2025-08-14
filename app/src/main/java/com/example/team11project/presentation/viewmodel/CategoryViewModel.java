package com.example.team11project.presentation.viewmodel;
import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.data.repository.CategoryRepositoryImpl;
import com.example.team11project.domain.repository.RepositoryCallback;


import java.util.ArrayList;
import java.util.List;

public class CategoryViewModel extends ViewModel {
    private final CategoryRepository categoryRepository;

    //promenljiva verzija
    private final MutableLiveData<List<Category>> _categories = new MutableLiveData<>();
    // nepromenljiva verzija za ui
    public final LiveData<List<Category>> categories = _categories;


    // za stanje ucitavanja
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // za greske
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;


    public CategoryViewModel(CategoryRepository repository) {
        this.categoryRepository = repository;
    }

    public void loadCategories(String userId) {
        _isLoading.setValue(true);
        categoryRepository.getCategories(userId, new RepositoryCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> result) {
                _categories.postValue(result);
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void createNewCategory(String name, String color, String userId) {
        _isLoading.setValue(true);

        Category category = new Category();
        category.setName(name);
        category.setColor(color);
        category.setUserId(userId);

        categoryRepository.addCategory(category, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Category> currentList = _categories.getValue();

                if (currentList == null) {
                    currentList = new ArrayList<>();
                }

                currentList.add(category);

                _categories.postValue(currentList);
                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void updateCategory(Category categoryToUpdate, String userId) {
        _isLoading.setValue(true);
        categoryRepository.updateCategory(categoryToUpdate, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Category> currentList = _categories.getValue();
                if (currentList != null) {
                    for (int i = 0; i < currentList.size(); i++) {
                        if (currentList.get(i).getId().equals(categoryToUpdate.getId())) {
                            currentList.set(i, categoryToUpdate);
                            break;
                        }
                    }
                    _categories.postValue(currentList);
                }
                _isLoading.postValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void deleteCategory(String categoryId, String userId) {
        _isLoading.setValue(true);
        categoryRepository.deleteCategory(categoryId, userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<Category> currentList = _categories.getValue();
                if (currentList != null) {
                    List<Category> updatedList = new ArrayList<>();
                    for (Category category : currentList) {
                        if (!category.getId().equals(categoryId)) {
                            updatedList.add(category);
                        }
                    }
                    _categories.postValue(updatedList);
                }
                _isLoading.postValue(false);
            }
            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CategoryViewModel.class)) {
                // Fabrika kreira repozitorijum...
                CategoryRepository repository = new CategoryRepositoryImpl(application);
                // ...i prosleđuje ga u konstruktor ViewModel-a.
                return (T) new CategoryViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
