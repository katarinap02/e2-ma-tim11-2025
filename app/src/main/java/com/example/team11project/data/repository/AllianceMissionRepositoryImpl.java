package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AllianceMissionRepositoryImpl implements AllianceMissionRepository {
    private LocalDataSource localDataSource;
    private RemoteDataSource remoteDataSource;
    private final ExecutorService databaseExecutor;

    public AllianceMissionRepositoryImpl(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.databaseExecutor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void createAllianceMission(AllianceMission mission, RepositoryCallback<String> callback) {
        if (mission.getAllianceId() == null || mission.getAllianceId().isEmpty()) {
            callback.onFailure(new Exception("Alliance ID je obavezan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Korak 1: Dodaj na Firebase
            remoteDataSource.createAllianceMission(mission, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    mission.setId(newId);

                    // Korak 2: Sačuvaj u lokalnu bazu
                    databaseExecutor.execute(() -> {
                        localDataSource.createAllianceMission(mission);
                        callback.onSuccess(newId);
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
    public void getActiveMissionByAllianceId(String allianceId, RepositoryCallback<AllianceMission> callback) {
        // Korak 1: Odmah dohvati lokalne podatke
        databaseExecutor.execute(() -> {
            AllianceMission localMission = localDataSource.getActiveMissionByAllianceId(allianceId);
            if (localMission != null) {
                callback.onSuccess(localMission);
            }
        });

        // Korak 2: Sinhronizuj sa Firebase-a
        remoteDataSource.getActiveMissionByAllianceId(allianceId, new RemoteDataSource.DataSourceCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission remoteMission) {
                databaseExecutor.execute(() -> {
                    if (remoteMission != null) {
                        // Ažuriraj lokalnu bazu
                        AllianceMission existingMission = localDataSource.getAllianceMissionById(remoteMission.getId());
                        if (existingMission != null) {
                            localDataSource.updateAllianceMission(remoteMission);
                        } else {
                            localDataSource.createAllianceMission(remoteMission);
                        }
                    }
                    // Pošalji fresh podatke
                    AllianceMission freshMission = localDataSource.getActiveMissionByAllianceId(allianceId);
                    callback.onSuccess(freshMission);
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void getAllianceMissionById(String missionId, RepositoryCallback<AllianceMission> callback) {
        databaseExecutor.execute(() -> {
            try {
                AllianceMission mission = localDataSource.getAllianceMissionById(missionId);
                if (mission != null) {
                    callback.onSuccess(mission);
                } else {
                    callback.onFailure(new Exception("Misija nije pronađena."));
                }
            } catch (Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void updateBossHp(String missionId, int newHp, RepositoryCallback<Void> callback) {
        databaseExecutor.execute(() -> {
            // Korak 1: Ažuriraj na Firebase
            remoteDataSource.updateBossHp(missionId, newHp, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Korak 2: Ažuriraj u lokalnoj bazi
                    databaseExecutor.execute(() -> {
                        localDataSource.updateBossHp(missionId, newHp);
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
    public void updateAllianceMission(AllianceMission mission, RepositoryCallback<Void> callback) {
        if (mission.getId() == null || mission.getId().isEmpty()) {
            callback.onFailure(new Exception("Mission ID je obavezan."));
            return;
        }

        databaseExecutor.execute(() -> {
            // Korak 1: Ažuriraj na Firebase
            remoteDataSource.updateAllianceMission(mission, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    // Korak 2: Ažuriraj u lokalnoj bazi
                    databaseExecutor.execute(() -> {
                        localDataSource.updateAllianceMission(mission);
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
