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
    public void addXp(User user, int xpEarned, RepositoryCallback<LevelInfo> callback) {
        LevelInfo levelInfo = user.getLevelInfo();
        int newXp = levelInfo.getXp() + xpEarned;
        int level = levelInfo.getLevel();
        int xpForNextLevel = levelInfo.getXpForNextLevel();
        int pp = levelInfo.getPp();

        while (newXp >= xpForNextLevel && level < 3) {
            level++;

            if (level == 1) {
                pp += 40;
            } else {
                pp = pp + (pp * 3) / 4;
            }
            xpForNextLevel = roundToNextHundred(xpForNextLevel * 2 + xpForNextLevel / 2);

            switch (level) {
                case 1:
                    levelInfo.setTitle(UserTitle.NAPREDNI);
                    break;
                case 2:
                    levelInfo.setTitle(UserTitle.PROGRESOR);
                    break;
                case 3:
                    levelInfo.setTitle(UserTitle.LEGENDICA);
                    break;
            }
        }

        levelInfo.setLevel(level);
        levelInfo.setXpForNextLevel(xpForNextLevel);
        levelInfo.setXp(newXp);
        levelInfo.setPp(pp);
        user.setLevelInfo(levelInfo);

        // asinhrono čuvanje
        remoteDataSource.updateUser(user, new RemoteDataSource.DataSourceCallback<Void>() {
            @Override
            public void onSuccess(Void unused) {
                databaseExecutor.execute(() -> {
                    // localDataSource.updateLevelInfo(levelInfo, user.getId());
                    callback.onSuccess(levelInfo); // sada vraćaš LevelInfo
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private int roundToNextHundred(int value) {
        return ((value + 99) / 100) * 100;
    }
}
