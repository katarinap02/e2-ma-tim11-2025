package com.example.team11project.domain.usecase;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CreateAllianceUseCase {

    private final AllianceRepository repository;
    private final UserRepository userRepository;

    public CreateAllianceUseCase(AllianceRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public void createAlliance(String leaderId, String allianceName, List<String> invitedUserIds, RepositoryCallback<Alliance> callback) {
        Alliance alliance = new Alliance();
        String allianceId = java.util.UUID.randomUUID().toString();
        alliance.setId(allianceId);
        alliance.setName(allianceName);
        alliance.setMissionActive(false);
        alliance.setMembers(new ArrayList<>());

        userRepository.getUserById(leaderId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User leader) {
                alliance.setLeader(leaderId);
                alliance.getMembers().add(leader.getId());

                leader.setCurrentAlliance(alliance);
                userRepository.updateUser(leader, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        repository.addAlliance(alliance, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void result) {
                                if (invitedUserIds == null || invitedUserIds.isEmpty()) {
                                    callback.onSuccess(alliance);
                                    return;
                                }

                                AtomicInteger counter = new AtomicInteger(invitedUserIds.size());

                                for (String userId : invitedUserIds) {
                                    userRepository.getUserById(userId, new RepositoryCallback<User>() {
                                        @Override
                                        public void onSuccess(User toUser) {
                                            if (toUser.getId() == null || toUser.getId().isEmpty()) {
                                                toUser.setId(userId);
                                            }

                                            AllianceInvite invite = new AllianceInvite();
                                            invite.setId(java.util.UUID.randomUUID().toString());
                                            invite.setAlliance(alliance);
                                            invite.setFromUser(leader);
                                            invite.setToUser(toUser);
                                            invite.setAccepted(false);
                                            invite.setResponded(false);

                                            repository.addAllianceInvite(invite, new RepositoryCallback<Void>() {
                                                @Override
                                                public void onSuccess(Void result) {
                                                    if (counter.decrementAndGet() == 0) {
                                                        callback.onSuccess(alliance);
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Exception e) {
                                                    e.printStackTrace();
                                                    if (counter.decrementAndGet() == 0) {
                                                        callback.onSuccess(alliance);
                                                    }
                                                }
                                            });
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            e.printStackTrace();
                                            if (counter.decrementAndGet() == 0) {
                                                callback.onSuccess(alliance);
                                            }
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onFailure(Exception e) {
                                callback.onFailure(e);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }
}
