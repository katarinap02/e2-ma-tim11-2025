package com.example.team11project.domain.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    // ovaj deo treba da bi znao da uradi rollback ako nije dobro
    interface CategoryCallback<T> {
        void onSuccess(T result);

        void onFailure(Exception e);

    }
    void addCategory(Category category, CategoryCallback<Void> callback);
    void getCategories(String userId, CategoryCallback<List<Category>> callback);
    
    void updateCategory(Category category, CategoryCallback<Void> callback);

    void deleteCategory(String categoryId, String userId, CategoryCallback<Void> callback);
    }
