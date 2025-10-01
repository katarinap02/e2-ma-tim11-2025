package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.repository.AllianceMissionRewardRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllianceMissionRewardRepositoryImpl implements AllianceMissionRewardRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public AllianceMissionRewardRepositoryImpl(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }
    @Override
    public void createAllianceMissionReward(AllianceMissionReward reward, RepositoryCallback<String> callback) {
        if (reward.getUserId() == null || reward.getUserId().isEmpty()) {
            callback.onFailure(new Exception("User ID je obavezan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Korak 1: Dodaj na Firebase
            remoteDataSource.createAllianceMissionReward(reward, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String rewardId) {
                    reward.setId(rewardId);

                    // Korak 2: Sačuvaj u lokalnu bazu
                    databaseExecutor.execute(() -> {
                        localDataSource.createAllianceMissionReward(reward);
                        callback.onSuccess(rewardId);
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
    public void getAllRewardsByUserId(String userId, RepositoryCallback<List<AllianceMissionReward>> callback) {
        // Korak 1: Odmah dohvati lokalne podatke
        databaseExecutor.execute(() -> {
            List<AllianceMissionReward> localRewards = localDataSource.getAllRewardsByUserId(userId);
            if (!localRewards.isEmpty()) {
                callback.onSuccess(localRewards);
            }
        });

        // Korak 2: Sinhronizuj sa Firebase-a
        remoteDataSource.getAllRewardsByUserId(userId, new RemoteDataSource.DataSourceCallback<List<AllianceMissionReward>>() {
            @Override
            public void onSuccess(List<AllianceMissionReward> remoteRewards) {
                databaseExecutor.execute(() -> {
                    // Obriši stare i dodaj nove
                    localDataSource.deleteAllRewardsForUser(userId);
                    for (AllianceMissionReward reward : remoteRewards) {
                        localDataSource.createAllianceMissionReward(reward);
                    }
                    // Pošalji fresh podatke
                    List<AllianceMissionReward> freshRewards = localDataSource.getAllRewardsByUserId(userId);
                    callback.onSuccess(freshRewards);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void getTotalBadgeCount(String userId, RepositoryCallback<Integer> callback) {
        databaseExecutor.execute(() -> {
            try {
                int badgeCount = localDataSource.getTotalBadgeCount(userId);
                callback.onSuccess(badgeCount);
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
