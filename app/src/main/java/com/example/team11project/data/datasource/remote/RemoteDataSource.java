package com.example.team11project.data.datasource.remote;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.team11project.domain.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

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


    public void addUser(User user, final DataSourceCallback<String> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        auth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();
                            user.setId(uid);

                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(user.getUsername())
                                    .build();
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        firebaseUser.sendEmailVerification()
                                                .addOnCompleteListener(verificationTask -> {
                                                    if (verificationTask.isSuccessful()) {
                                                        db.collection(USERS_COLLECTION)
                                                                .document(uid)
                                                                .set(user)
                                                                .addOnSuccessListener(aVoid -> callback.onSuccess(uid))
                                                                .addOnFailureListener(callback::onFailure);
                                                    } else {
                                                        callback.onFailure(verificationTask.getException());
                                                        firebaseUser.delete();
                                                    }
                                                });
                                    });
                        } else {
                            callback.onFailure(new Exception("FirebaseUser je null nakon kreiranja."));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }

    public void refreshUserVerificationStatus(User user, final DataSourceCallback<Void> callback) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            firebaseUser.reload().addOnCompleteListener(task -> {
                if (firebaseUser.isEmailVerified()) {
                    user.setVerified(true);
                    db.collection(USERS_COLLECTION)
                            .document(user.getId())
                            .set(user, SetOptions.merge())
                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure);
                } else {
                    callback.onFailure(new Exception("User email not verified yet"));
                }
            });
        } else {
            callback.onFailure(new Exception("FirebaseUser is null"));
        }
    }

    public void login(String email, String password, final DataSourceCallback<User> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        Log.d("LoginDebug", "Searching for email: '" + email + "'");

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = auth.getCurrentUser();

                        if (firebaseUser == null) {
                            callback.onFailure(new Exception("Greška prilikom prijave, pokušajte ponovo."));
                            return;
                        }

                        // 2. Sada proveri da li je email verifikovan
                        if (firebaseUser.isEmailVerified()) {
                            // 3. Ako jeste, dohvati ostatak podataka o korisniku iz Firestore baze
                            db.collection(USERS_COLLECTION)
                                    .document(firebaseUser.getUid()) // Koristi jedinstveni UID korisnika
                                    .get()
                                    .addOnCompleteListener(firestoreTask -> {
                                        if (firestoreTask.isSuccessful() && firestoreTask.getResult() != null) {
                                            User user = firestoreTask.getResult().toObject(User.class);
                                            if (user != null) {
                                                user.setId(firebaseUser.getUid()); // Osiguraj da je ID postavljen
                                                callback.onSuccess(user);
                                            } else {
                                                callback.onFailure(new Exception("Korisnički podaci nisu pronađeni."));
                                            }
                                        } else {
                                            callback.onFailure(firestoreTask.getException());
                                        }
                                    });
                        } else {
                            // 4. Ako email NIJE verifikovan, vrati tačnu poruku o grešci
                            auth.signOut(); // Izloguj korisnika jer nije verifikovan
                            callback.onFailure(new Exception("Morate prvo da aktivirate nalog putem linka u mejlu"));
                        }
                    } else {
                        // Ako prijava ne uspe (npr. pogrešna lozinka), Firebase će vratiti odgovarajuću grešku
                        callback.onFailure(authTask.getException());
                    }
                });
    }

}
