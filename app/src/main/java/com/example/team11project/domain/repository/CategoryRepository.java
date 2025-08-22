package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Category;

import java.util.List;

public interface CategoryRepository {
    // ovaj deo treba da bi znao da uradi rollback ako nije dobro

    void addCategory(Category category, RepositoryCallback<Void> callback);
    void getCategories(String userId, RepositoryCallback<List<Category>> callback);

    void getCategoryById(String categoryId, String userId, RepositoryCallback<Category> callback);

    void updateCategory(Category category, RepositoryCallback<Void> callback);

    void deleteCategory(String categoryId, String userId, RepositoryCallback<Void> callback);
    }
