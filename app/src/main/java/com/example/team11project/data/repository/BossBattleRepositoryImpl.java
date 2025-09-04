package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BossBattleRepositoryImpl implements BossBattleRepository {
    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public  BossBattleRepositoryImpl(Context context)
    {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addBattle(BossBattle battle, RepositoryCallback<Void> callback) {
        if (battle.getAttacksUsed() < 0) {
            callback.onFailure(new Exception("Broj napada ne može biti negativan."));
            return;
        }

        databaseExecutor.execute(() -> {
            remoteDataSource.addBossBattle(battle, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    battle.setId(newId);
                    databaseExecutor.execute(() -> {
                        localDataSource.addBossBattle(battle);
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
    public void getBattles(String userId, RepositoryCallback<List<BossBattle>> callback) {
        databaseExecutor.execute(() -> {
            List<BossBattle> localBattles = localDataSource.getAllBossBattles(userId);
            callback.onSuccess(localBattles);
        });

        remoteDataSource.getAllBossBattles(userId, new RemoteDataSource.DataSourceCallback<List<BossBattle>>() {
            @Override
            public void onSuccess(List<BossBattle> remoteBattles) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteAllBossBattlesForUser(userId);
                    for (BossBattle b : remoteBattles) {
                        localDataSource.addBossBattle(b);
                    }
                    callback.onSuccess(localDataSource.getAllBossBattles(userId));
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("BossBattle sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateBattle(BossBattle battle, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            remoteDataSource.updateBossBattle(battle, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    databaseExecutor.execute(() -> {
                        localDataSource.updateBossBattle(battle);
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
    public void getBattleByUserAndBossAndLevel(String userId, String bossId, int level, RepositoryCallback<BossBattle> callback) {
        databaseExecutor.execute(() -> {
            // Prvo pokušamo iz remote data source-a
            remoteDataSource.getBattleByUserAndBossAndLevel(userId, bossId, level, new RemoteDataSource.DataSourceCallback<BossBattle>() {
                @Override
                public void onSuccess(BossBattle bossBattle) {
                    if (bossBattle != null) {
                        databaseExecutor.execute(() -> {
                            callback.onSuccess(bossBattle);
                        });
                    } else {
                        databaseExecutor.execute(() -> {
                            BossBattle localBossBattle = localDataSource.getBattleByUserAndBossAndLevel(userId, bossId, level);
                            callback.onSuccess(localBossBattle);
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    databaseExecutor.execute(() -> {
                        BossBattle localBossBattle = localDataSource.getBattleByUserAndBossAndLevel(userId, bossId, level);
                        if (localBossBattle != null) {
                            // Pronađen u lokalnoj bazi
                            callback.onSuccess(localBossBattle);
                        } else {
                            // Nije pronađen nigde
                            callback.onFailure(e);
                        }
                    });
                }
            });
        });
    }
}
