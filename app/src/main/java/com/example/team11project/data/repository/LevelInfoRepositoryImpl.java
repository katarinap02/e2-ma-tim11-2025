package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.UserTitle;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;

public class LevelInfoRepositoryImpl implements LevelInfoRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final Context context;
    private final ExecutorService databaseExecutor;


    public LevelInfoRepositoryImpl(Context context) {
        this.context = context;
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addXp(User user, int xpEarned, RepositoryCallback<Void> callback){
        LevelInfo levelInfo = user.getLevelInfo();
        int newXp = levelInfo.getXp() + xpEarned;
        int level = levelInfo.getLevel();
        int xpForNextLevel = levelInfo.getXpForNextLevel();
        int pp = levelInfo.getPp();

        while(newXp >= xpForNextLevel && level < 3){
            level++;

            if(level == 1){
                pp += 40;
            }
            else{
                pp = pp + (pp * 3) / 4;
            }
            xpForNextLevel = roundToNextHundered(xpForNextLevel * 2 + xpForNextLevel / 2);

            switch(level){
                case 1: levelInfo.setTitle(UserTitle.NAPREDNI); break;
                case 2: levelInfo.setTitle(UserTitle.PROGRESOR); break;
                case 3: levelInfo.setTitle(UserTitle.LEGENDICA); break;
            }
        }
        levelInfo.setLevel(level);
        levelInfo.setXpForNextLevel(xpForNextLevel);
        levelInfo.setXp(newXp);
        user.setLevelInfo(levelInfo);
        levelInfo.setPp(pp);

        remoteDataSource.updateUser(user, new RemoteDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                databaseExecutor.execute(() -> {
                  //  localDataSource.updateLevelInfo(levelInfo, user.getId());
                    callback.onSuccess(null);
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private int roundToNextHundered(int value){
        return ((value + 99) / 100) * 100;
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



}
