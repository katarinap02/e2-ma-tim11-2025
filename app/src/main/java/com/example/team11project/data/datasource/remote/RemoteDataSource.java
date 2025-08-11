package com.example.team11project.data.datasource.remote;
import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RemoteDataSource {

    private FirebaseFirestore db;
    private static final String USERS_COLLECTION = "users";
    private static final String TASKS_COLLECTION = "tasks";
    private static final String CATEGORIES_COLLECTION = "categories";

    public RemoteDataSource() {
        this.db = FirebaseFirestore.getInstance();
    }

    public interface DataSourceCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception e);
    }

    public void addTask(Task task, final DataSourceCallback<String> callback) {
        // Struktura: /users/{userId}/tasks/{taskId}
        db.collection(USERS_COLLECTION).document(task.getUserId())
                .collection(TASKS_COLLECTION)
                .add(task) // Firestore automatski konvertuje POJO klasu
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllTasks(String userId, final DataSourceCallback<List<Task>> callback) {
        getTasksCollection(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task> taskList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Task t = document.toObject(Task.class);
                            t.setId(document.getId()); // Ručno postavljamo ID dokumenta
                            taskList.add(t);
                        }
                        callback.onSuccess(taskList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void deleteTask(String taskId, String userId, final DataSourceCallback<Void> callback) {
        getTasksCollection(userId).document(taskId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
    public void updateTask(Task task, final DataSourceCallback<Void> callback) {
        getTasksCollection(task.getUserId()).document(task.getId())
                .set(task) // set() će ili ažurirati postojeći ili kreirati novi ako ne postoji
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void addCategory(Category category, final DataSourceCallback<String> callback) {
        // Struktura: /users/{userId}/categories/{categoryId}
        db.collection(USERS_COLLECTION).document(category.getUserId())
                .collection(CATEGORIES_COLLECTION)
                .add(category)
                .addOnSuccessListener(documentReference -> callback.onSuccess(documentReference.getId()))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void getAllCategories(String userId, final DataSourceCallback<List<Category>> callback) {
        getCategoriesCollection(userId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Category> categoryList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Category c = document.toObject(Category.class);
                            c.setId(document.getId()); // Ručno postavljamo ID
                            categoryList.add(c);
                        }
                        callback.onSuccess(categoryList);
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void updateCategory(Category category, final DataSourceCallback<Void> callback) {
        getCategoriesCollection(category.getUserId()).document(category.getId())
                .set(category)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void deleteCategory(String categoryId, String userId, final DataSourceCallback<Void> callback) {
        getCategoriesCollection(userId).document(categoryId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    private CollectionReference getTasksCollection(String userId) {
        return db.collection(USERS_COLLECTION).document(userId).collection(TASKS_COLLECTION);
    }

    private CollectionReference getCategoriesCollection(String userId) {
        return db.collection(USERS_COLLECTION).document(userId).collection(CATEGORIES_COLLECTION);
    }




    }
