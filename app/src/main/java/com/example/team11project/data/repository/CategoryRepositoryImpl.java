package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryRepositoryImpl implements CategoryRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;

    // Koristimo jednog izvršioca da bismo osigurali da se operacije sa bazom izvršavaju redom
    private final ExecutorService databaseExecutor;


    public  CategoryRepositoryImpl(Context context)
    {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addCategory(Category category, RepositoryCallback<Void> callback) {
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            callback.onFailure(new Exception("Naziv kategorije ne sme biti prazan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Pravilo 2: Proveri da li je boja već u upotrebi LOKALNO (brza provera)
            if (localDataSource.isColorUsed(category.getColor(), category.getUserId())) {
                callback.onFailure(new Exception("Izabrana boja se već koristi."));
                return;
            }

                // Korak 1: Pokušaj da dodaš na Firebase
                remoteDataSource.addCategory(category, new RemoteDataSource.DataSourceCallback<String>() {
                    @Override
                    public void onSuccess(String newId) {
                        // Uspeh! Postavi ID koji je Firebase generisao
                        category.setId(newId);

                        // Korak 2: Sada sačuvaj i u lokalnu SQLite bazu
                        databaseExecutor.execute(() -> {
                            localDataSource.addCategory(category);
                            callback.onSuccess(null); // Javi finalni uspeh sa novim ID-em
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
    });
}

    @Override
    public void getCategories(String userId, RepositoryCallback<List<Category>> callback) {
        // Korak 1: Odmah u pozadini dohvati lokalne podatke i pošalji ih UI-ju
        databaseExecutor.execute(() -> {
            List<Category> localCategories = localDataSource.getAllCategories(userId);
            callback.onSuccess(localCategories); // Odmah prikazujemo šta imamo
        });

        // Korak 2: Pokreni sinhronizaciju sa Firebase-a
        remoteDataSource.getAllCategories(userId, new RemoteDataSource.DataSourceCallback<List<Category>>() {
            @Override
            public void onSuccess(List<Category> remoteCategories) {
                // Dobili smo sveže podatke, sada ih sinhronizuj sa lokalnom bazom
                databaseExecutor.execute(() -> {
                    // Obriši sve stare i ubaci sve nove.
                    localDataSource.deleteAllCategoriesForUser(userId);
                    for (Category cat : remoteCategories) {
                        localDataSource.addCategory(cat);
                    }
                    // Nakon sinhronizacije, ponovo pošalji sveže podatke UI-ju
                    List<Category> freshLocalCategories = localDataSource.getAllCategories(userId);
                    callback.onSuccess(freshLocalCategories);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateCategory(Category category, RepositoryCallback<Void> callback) {
//  Validacija
        if (category.getName() == null || category.getName().trim().isEmpty()) {
            callback.onFailure(new Exception("Naziv kategorije ne sme biti prazan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Da li novu boju već koristi druga kategorija
            if (localDataSource.isColorUsed(category.getColor(), category.getUserId())) {
                callback.onFailure(new Exception("Izabrana boja se već koristi."));
                return;
            }

            // Ažuriranje na Firebase-u
            remoteDataSource.updateCategory(category, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Korak 2: Ažuriranje i u lokalnoj bazi
                    databaseExecutor.execute(() -> {
                        localDataSource.updateCategory(category);
                        callback.onSuccess(null);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        });
    }

    @Override
    public void deleteCategory(String categoryId, String userId, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            //Ne dozvoliti brisanje kategorije ako je neki zadatak koristi
            if (localDataSource.isCategoryInUse(categoryId, userId)) {
                callback.onFailure(new Exception("Nije moguće obrisati kategoriju jer se koristi u zadacima."));
                return;
            }

            //Brisanje sa Firebase-a
            remoteDataSource.deleteCategory(categoryId, userId, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Korak 2:Brisanje iz lokalne baze
                    databaseExecutor.execute(() -> {
                        localDataSource.deleteCategory(categoryId, userId);
                        callback.onSuccess(null);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        });
    }

}
