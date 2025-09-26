package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.BossReward;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BossRewardRepositoryImpl implements BossRewardRepository {
    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public  BossRewardRepositoryImpl(Context context)
    {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addReward(BossReward reward, RepositoryCallback<Void> callback) {
        if (reward.getCoinsEarned() < 0) {
            callback.onFailure(new Exception("Broj osvojenih novčića ne može biti negativan."));
            return;
        }

        databaseExecutor.execute(() -> {
            remoteDataSource.addBossReward(reward, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    reward.setId(newId);
                    databaseExecutor.execute(() -> {
                        localDataSource.addBossReward(reward);
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
    public void getRewards(String userId, RepositoryCallback<List<BossReward>> callback) {
        databaseExecutor.execute(() -> {
            List<BossReward> localRewards = localDataSource.getAllBossRewards(userId);
            callback.onSuccess(localRewards);
        });

        remoteDataSource.getAllBossRewards(userId, new RemoteDataSource.DataSourceCallback<List<BossReward>>() {
            @Override
            public void onSuccess(List<BossReward> remoteRewards) {
                databaseExecutor.execute(() -> {
                    localDataSource.deleteAllBossRewardsForUser(userId);
                    for (BossReward r : remoteRewards) {
                        localDataSource.addBossReward(r);
                    }
                    callback.onSuccess(localDataSource.getAllBossRewards(userId));
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("BossReward sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateReward(BossReward reward, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            remoteDataSource.updateBossReward(reward, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    databaseExecutor.execute(() -> {
                        localDataSource.updateBossReward(reward);
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
    public void getRewardByUserAndBossAndLevel(String userId, String bossId, int level, RepositoryCallback<BossReward> callback) {
        databaseExecutor.execute(() -> {
            // Prvo pokušamo iz remote data source-a
            remoteDataSource.getRewardByUserAndBossAndLevel(userId, bossId, level, new RemoteDataSource.DataSourceCallback<BossReward>() {
                @Override
                public void onSuccess(BossReward bossReward) {
                    if (bossReward != null) {
                        databaseExecutor.execute(() -> {
                            callback.onSuccess(bossReward);
                        });
                    } else {
                        databaseExecutor.execute(() -> {
                            BossReward localBossReward = localDataSource.getRewardByUserAndBossAndLevel(userId, bossId, level);
                            callback.onSuccess(localBossReward);
                        });
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    databaseExecutor.execute(() -> {
                        BossReward localBossReward = localDataSource.getRewardByUserAndBossAndLevel(userId, bossId, level);
                        if (localBossReward != null) {
                            // Pronađen u lokalnoj bazi
                            callback.onSuccess(localBossReward);
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
