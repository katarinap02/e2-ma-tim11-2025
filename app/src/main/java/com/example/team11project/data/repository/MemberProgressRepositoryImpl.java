package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.repository.MemberProgressRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MemberProgressRepositoryImpl implements MemberProgressRepository {

    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public MemberProgressRepositoryImpl(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }
    @Override
    public void updateMemberProgress(MemberProgress progress, RepositoryCallback<Void> callback) {
        if (progress.getMissionId() == null || progress.getMissionId().isEmpty()) {
            callback.onFailure(new Exception("Mission ID je obavezan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Korak 1: Ažuriraj na Firebase
            remoteDataSource.updateMemberProgress(progress, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Korak 2: Ažuriraj u lokalnoj bazi
                    databaseExecutor.execute(() -> {
                        localDataSource.updateMemberProgress(progress);
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
    public void getMemberProgressByUserId(String missionId, String userId, RepositoryCallback<MemberProgress> callback) {
        databaseExecutor.execute(() -> {
            try {
                MemberProgress progress = localDataSource.getMemberProgressByUserId(missionId, userId);
                if (progress != null) {
                    callback.onSuccess(progress);
                } else {
                    callback.onFailure(new Exception("Progress nije pronađen."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }


}
