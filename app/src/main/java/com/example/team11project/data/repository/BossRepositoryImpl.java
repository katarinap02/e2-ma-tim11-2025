package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BossRepositoryImpl implements BossRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public  BossRepositoryImpl(Context context)
    {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addBoss(Boss boss, RepositoryCallback<Void> callback) {
        if (boss.getLevel() <= 0) {
            callback.onFailure(new Exception("Level bosa mora biti veći od 0."));
            return;
        }

        databaseExecutor.execute(() -> {
            remoteDataSource.addBoss(boss, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    boss.setId(newId);
                    databaseExecutor.execute(() -> {
                        localDataSource.addBoss(boss);
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
    public void getBosses(String userId, RepositoryCallback<List<Boss>> callback) {
        databaseExecutor.execute(() -> {
            List<Boss> localBosses = localDataSource.getAllBosses(userId);
            callback.onSuccess(localBosses);
        });

        remoteDataSource.getAllBosses(userId, new RemoteDataSource.DataSourceCallback<List<Boss>>() {
            @Override
            public void onSuccess(List<Boss> remoteBosses) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteAllBossesForUser(userId);
                    for (Boss b : remoteBosses) {
                        localDataSource.addBoss(b);
                    }
                    callback.onSuccess(localDataSource.getAllBosses(userId));
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Boss sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateBoss(Boss boss, RepositoryCallback<Void> callback) {
        if (boss.getLevel() <= 0) {
            callback.onFailure(new Exception("Level bosa mora biti veći od 0."));
            return;
        }

        databaseExecutor.execute(() -> {
            remoteDataSource.updateBoss(boss, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    databaseExecutor.execute(() -> {
                        localDataSource.updateBoss(boss);
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
