package com.example.team11project.data.repository;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.LoginResult;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepositoryImpl implements UserRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;

    private final ExecutorService databaseExecutor;
    private final Context context;

    public UserRepositoryImpl(Context context){
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
        this.context = context;
    }

    @Override
    public void addUser(User user, RepositoryCallback<Void> callback){
        if(user.getEmail() == null || user.getEmail().trim().isEmpty()){
            callback.onFailure(new Exception("Email cannot be empty"));
            return;
        }

        user.setVerified(false);
        remoteDataSource.addUser(user, new RemoteDataSource.DataSourceCallback<String>() {
            @Override
            public void onSuccess(String newId) {
                if (newId == null) {
                    Log.e("Register", "Firebase je vratio null ID za korisnika!");
                } else {
                    Log.d("Register", "Firebase ID: " + newId);
                }
                user.setId(newId);
                Log.d("Register", "User ID: " + user.getId());


                databaseExecutor.execute(() -> {
                    localDataSource.addUser(user);
                    callback.onSuccess(null);
                });


            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    public void getUserById(String userId, RepositoryCallback<User> callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onFailure(new Exception("User ID je null ili prazan."));
            return;
        }

        remoteDataSource.getUserById(userId, new RemoteDataSource.DataSourceCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    databaseExecutor.execute(() -> {
                        callback.onSuccess(user);
                    });
                } else {
                    callback.onFailure(new Exception("Korisnik ne postoji."));
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
    @Override
    public LoginResult login(String email, String password, RepositoryCallback<User> callback){
        LoginResult result = new LoginResult();

        remoteDataSource.login(email, password, new RemoteDataSource.DataSourceCallback<User>() {
            @Override
            public void onSuccess(User data) {

                SharedPreferences prefs = context.getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                prefs.edit()
                        .putString("userId", data.getId())
                        .putString("sessionToken", UUID.randomUUID().toString())
                        .apply();

                callback.onSuccess(data);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
        return result;
    }

    @Override
    public void getAllUsers(RepositoryCallback<List<User>> callback){
        databaseExecutor.execute(() -> {
            List<User> localUsers = localDataSource.getAllUsers();
        });

        remoteDataSource.getAllUsers(new RemoteDataSource.DataSourceCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> remoteUsers) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteAllUsers();
                    for (User user : remoteUsers) {
                        localDataSource.addUser(user);
                    }
                    List<User> freshLocalUsers = localDataSource.getAllUsers();
                    callback.onSuccess(freshLocalUsers);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updatePassword(String userId, String newPassword, RepositoryCallback<Void> callback) {
        if (newPassword == null || newPassword.trim().isEmpty()) {
            callback.onFailure(new Exception("Lozinka ne sme biti prazna."));
            return;
        }

        databaseExecutor.execute(() -> {
            Log.d("DEBUG", "Repository: calling remote updatePassword");

            remoteDataSource.updatePassword(userId, newPassword, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Log.d("DEBUG", "Repository: remote update success");

                    databaseExecutor.execute(() -> {
                        int count = localDataSource.updatePassword(userId, newPassword);
                        Log.d("DEBUG", "Repository: local update count = " + count);
                        callback.onSuccess(null);
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("DEBUG", "Repository: remote update failed", e);
                    callback.onFailure(e);
                }
            });
        });
    }

    @Override
    public void getPotionsByUserId(String userId, RepositoryCallback<List<Potion>> callback) {
        databaseExecutor.execute(() -> {
            List<Potion> localPotions = localDataSource.getPotionsByUserId(userId);
            if (localPotions != null && !localPotions.isEmpty()) {
                callback.onSuccess(localPotions);
            }
        });

        remoteDataSource.getPotionsByUserId(userId, new RemoteDataSource.DataSourceCallback<List<Potion>>() {
            @Override
            public void onSuccess(List<Potion> remotePotions) {
                databaseExecutor.execute(() -> {
                    localDataSource.deletePotion(userId);
                    for (Potion potion : remotePotions) {
                        localDataSource.addPotion(userId, potion);
                    }
                    List<Potion> freshLocalPotions = localDataSource.getPotionsByUserId(userId);
                    callback.onSuccess(freshLocalPotions);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync potions failed: " + e.getMessage());
                databaseExecutor.execute(() -> {
                    List<Potion> fallbackPotions = localDataSource.getPotionsByUserId(userId);
                    callback.onSuccess(fallbackPotions);
                });
            }
        });
    }


    @Override
    public void getWeaponsByUserId(String userId, RepositoryCallback<List<Weapon>> callback) {
        databaseExecutor.execute(() -> {
            List<Weapon> localWeapons = localDataSource.getWeaponsByUserId(userId);
            if (localWeapons != null && !localWeapons.isEmpty()) {
                callback.onSuccess(localWeapons);
            }
        });

        remoteDataSource.getWeaponByUserId(userId, new RemoteDataSource.DataSourceCallback<List<Weapon>>() {
            @Override
            public void onSuccess(List<Weapon> remoteWeapons) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteWeapon(userId);
                    for (Weapon weapon : remoteWeapons) {
                        localDataSource.addWeapon(userId, weapon);
                    }
                    List<Weapon> freshLocalWeapons = localDataSource.getWeaponsByUserId(userId);
                    callback.onSuccess(freshLocalWeapons);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync weapons failed: " + e.getMessage());
                databaseExecutor.execute(() -> {
                    List<Weapon> fallbackWeapons = localDataSource.getWeaponsByUserId(userId);
                    callback.onSuccess(fallbackWeapons);
                });
            }
        });
    }

    @Override
    public void getClothingByUserId(String userId, RepositoryCallback<List<Clothing>> callback) {
        databaseExecutor.execute(() -> {
            List<Clothing> localClothing = localDataSource.getClothingByUserId(userId);
            if (localClothing != null && !localClothing.isEmpty()) {
                callback.onSuccess(localClothing);
            }
        });

        remoteDataSource.getClothingByUserId(userId, new RemoteDataSource.DataSourceCallback<List<Clothing>>() {
            @Override
            public void onSuccess(List<Clothing> remoteClothing) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteClothing(userId);
                    for (Clothing clothing : remoteClothing) {
                        localDataSource.addClothing(userId, clothing);
                    }
                    List<Clothing> freshLocalClothing = localDataSource.getClothingByUserId(userId);
                    callback.onSuccess(freshLocalClothing);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync clothing failed: " + e.getMessage());
                databaseExecutor.execute(() -> {
                    List<Clothing> fallbackClothing = localDataSource.getClothingByUserId(userId);
                    callback.onSuccess(fallbackClothing);
                });
            }
        });
    }

    @Override
    public void updateUser(User user, RepositoryCallback<Void> callback) {
        remoteDataSource.updateUser(user, new RemoteDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                localDataSource.saveUser(user);
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


}
