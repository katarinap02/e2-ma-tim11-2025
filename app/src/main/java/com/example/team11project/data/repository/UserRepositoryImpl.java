package com.example.team11project.data.repository;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.LoginResult;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

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

    @Override
    public void getUser(String userId, RepositoryCallback<Void> callback){

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

}
