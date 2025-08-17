package com.example.team11project.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserRepositoryImpl implements UserRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;

    private final ExecutorService databaseExecutor;

    public UserRepositoryImpl(Context context){
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addUser(User user, RepositoryCallback<Void> callback){
        if(user.getMail() == null || user.getMail().trim().isEmpty()){
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

}
