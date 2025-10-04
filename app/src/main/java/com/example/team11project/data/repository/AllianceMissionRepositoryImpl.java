package com.example.team11project.data.repository;

import android.content.Context;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.model.MemberProgress;
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


        // Korak 1: Pokušaj dohvatiti iz lokalne baze (ali NE šalji još callback)
        databaseExecutor.execute(() -> {
            AllianceMission localMission = localDataSource.getActiveMissionByAllianceId(allianceId);
            if (localMission != null) {
                if (localMission.getMemberProgress() != null) {
                }
                // ✅ Pošalji lokalne podatke KAO CACHE (brzi prikaz)
                callback.onSuccess(localMission);
            } else {
            }
        });

        // Korak 2: Uvek dohvati fresh podatke sa Firebase-a
        remoteDataSource.getActiveMissionByAllianceId(allianceId, new RemoteDataSource.DataSourceCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission remoteMission) {
                if (remoteMission == null) {
                    callback.onSuccess(null);
                    return;
                }

                if (remoteMission.getMemberProgress() != null) {
                } else {

                }

                // Sačuvaj u lokalnu bazu
                databaseExecutor.execute(() -> {
                    AllianceMission existingMission = localDataSource.getAllianceMissionById(remoteMission.getId());
                    if (existingMission != null) {
                        localDataSource.updateAllianceMission(remoteMission);
                    } else {
                        localDataSource.createAllianceMission(remoteMission);
                    }

                    // ✅ Pošalji FRESH podatke sa Firebase-a
                    callback.onSuccess(remoteMission);
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
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
        // 1. Odmah vrati lokalni zbir (ako postoji)
        databaseExecutor.execute(() -> {
            int localCount = localDataSource.getTotalBadgeCount(userId);
            if (localCount > 0) {
                callback.onSuccess(localCount);
            }
        });

        // 2. Povuci sve nagrade sa Firebase-a
        remoteDataSource.getAllRewardsByUserId(userId, new RemoteDataSource.DataSourceCallback<List<AllianceMissionReward>>() {
            @Override
            public void onSuccess(List<AllianceMissionReward> remoteRewards) {
                databaseExecutor.execute(() -> {
                    // Očisti stare i dodaj nove
                    localDataSource.deleteAllRewardsForUser(userId);
                    for (AllianceMissionReward reward : remoteRewards) {
                        localDataSource.createAllianceMissionReward(reward);
                    }

                    // Izračunaj fresh badge count
                    int freshCount = 0;
                    for (AllianceMissionReward reward : remoteRewards) {
                        if (reward != null) {
                            freshCount += reward.getBadgeCount();
                        }
                    }

                    callback.onSuccess(freshCount);
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


}
