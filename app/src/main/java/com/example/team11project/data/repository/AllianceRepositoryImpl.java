package com.example.team11project.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.team11project.data.datasource.local.LocalDataSource;
import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.UUID;

public class AllianceRepositoryImpl implements AllianceRepository {

    private final LocalDataSource localDataSource;
    private final RemoteDataSource remoteDataSource;
    private final ExecutorService executor;

    public AllianceRepositoryImpl(Context context) {
        this.localDataSource = new LocalDataSource(context);
        this.remoteDataSource = new RemoteDataSource();
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void addAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        if (alliance.getId() == null || alliance.getId().isEmpty()) {
            alliance.setId(UUID.randomUUID().toString());
        }
        if (alliance.getLeader() == null) {
            callback.onFailure(new Exception("Alliance must have a leaderId"));
            return;
        }

        executor.execute(() -> {
            remoteDataSource.addAlliance(alliance, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    alliance.setId(newId);
                    executor.execute(() -> {
                        localDataSource.addAlliance(alliance);
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
    public void getAllAlliances(String userId, RepositoryCallback<List<Alliance>> callback) {
        executor.execute(() -> {
            List<Alliance> localAlliances = localDataSource.getAllAlliances(userId);
            callback.onSuccess(localAlliances);
        });

        remoteDataSource.getAllAlliances(userId, new RemoteDataSource.DataSourceCallback<List<Alliance>>() {
            @Override
            public void onSuccess(List<Alliance> remoteAlliances) {
                executor.execute(() -> {
                    localDataSource.deleteAllAlliancesForUser(userId);
                    for (Alliance a : remoteAlliances) {
                        localDataSource.addAlliance(a);
                    }
                    callback.onSuccess(localDataSource.getAllAlliances(userId));
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("Alliance sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void getAllianceById(String userId, String allianceId, RepositoryCallback<Alliance> callback) {
        remoteDataSource.getAllianceById(userId, allianceId, new RemoteDataSource.DataSourceCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                // loguj sve što dobiješ
                Log.d("DEBUG", "Remote alliance loaded: " + alliance.toString());
                callback.onSuccess(alliance); // ovo će update-ovati LiveData
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    @Override
    public void updateAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            remoteDataSource.updateAlliance(alliance, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    executor.execute(() -> {
                        localDataSource.updateAlliance(alliance);
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
    public void deleteAlliance(String allianceId, String userId, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            remoteDataSource.deleteAlliance(allianceId, userId, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    executor.execute(() -> {
                        localDataSource.deleteAlliance(allianceId, userId);
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
    public void addAllianceInvite(AllianceInvite invite, RepositoryCallback<Void> callback) {
        if (invite.getId() == null || invite.getId().isEmpty()) {
            invite.setId(UUID.randomUUID().toString());
        }
        if (invite.getToUser().getId() == null) {
            callback.onFailure(new Exception("Invite must have toUserId"));
            return;
        }

        executor.execute(() -> {
            remoteDataSource.addAllianceInvite(invite, new RemoteDataSource.DataSourceCallback<String>() {
                @Override
                public void onSuccess(String newId) {
                    invite.setId(newId);
                    executor.execute(() -> {
                        localDataSource.addAllianceInvite(invite);
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
    public void getAllAllianceInvites(String userId, RepositoryCallback<List<AllianceInvite>> callback) {
        executor.execute(() -> {
            List<AllianceInvite> localInvites = localDataSource.getAllInvites(userId);
            callback.onSuccess(localInvites);
        });

        remoteDataSource.getAllAllianceInvites(userId, new RemoteDataSource.DataSourceCallback<List<AllianceInvite>>() {
            @Override
            public void onSuccess(List<AllianceInvite> remoteInvites) {
                executor.execute(() -> {
                    localDataSource.deleteAllAllianceInvitesForUser(userId);
                    for (AllianceInvite i : remoteInvites) {
                        localDataSource.addAllianceInvite(i);
                    }
                    callback.onSuccess(localDataSource.getAllInvites(userId));
                });
            }

            @Override
            public void onFailure(Exception e) {
                System.out.println("AllianceInvite sync failed: " + e.getMessage());
            }
        });
    }

    @Override
    public void updateAllianceInvite(AllianceInvite invite, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            remoteDataSource.updateAllianceInvite(invite, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    executor.execute(() -> {
                        localDataSource.updateAllianceInvite(invite);
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
    public void deleteAllianceInvite(String inviteId, String userId, RepositoryCallback<Void> callback) {
        executor.execute(() -> {
            remoteDataSource.deleteAllianceInvite(inviteId, userId, new RemoteDataSource.DataSourceCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    executor.execute(() -> {
                        localDataSource.deleteAllianceInvite(inviteId, userId);
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
