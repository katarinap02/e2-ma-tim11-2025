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
        if (userId == null || userId.trim().isEmpty()) {
            callback.onFailure(new Exception("UserID is null or empty"));
            return;
        }

        // Dohvat sa remote baze
        remoteDataSource.getAllBossBattles(userId, new RemoteDataSource.DataSourceCallback<List<BossBattle>>() {
            @Override
            public void onSuccess(List<BossBattle> remoteBattles) {
                databaseExecutor.execute(() -> {
                    // Ažuriraj lokalnu bazu: dodaj ili update svaki battle
                    for (BossBattle battle : remoteBattles) {
                        localDataSource.updateBossBattle(battle); // updateBossBattle treba da radi insert ako ne postoji
                    }

                    // Vrati callback sa sinhronizovanim battle-ima
                    List<BossBattle> syncedBattles = localDataSource.getAllBossBattles(userId);
                    callback.onSuccess(syncedBattles);
                });
            }

            @Override
            public void onFailure(Exception e) {
                // Ako remote fail-uje, barem vrati lokalne battle-e
                databaseExecutor.execute(() -> {
                    List<BossBattle> localBattles = localDataSource.getAllBossBattles(userId);
                    callback.onSuccess(localBattles);
                });
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
