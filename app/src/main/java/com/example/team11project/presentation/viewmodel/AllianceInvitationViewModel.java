package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceInvite;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.List;
import java.util.concurrent.Executor;

public class AllianceInvitationViewModel extends ViewModel {

    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;
    private final Executor executor;

    private final MutableLiveData<String> successMessage = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<List<AllianceInvite>> invites = new MutableLiveData<>();
    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();


    public AllianceInvitationViewModel(AllianceRepository repository, Executor executor, UserRepository userRepository) {
        this.allianceRepository = repository;
        this.userRepository = userRepository;
        this.executor = executor;
    }

    public LiveData<String> getSuccessMessage() { return successMessage; }
    public LiveData<String> getError() { return error; }
    public LiveData<List<AllianceInvite>> getInvites() { return invites; }

    public void loadInvites(String userId) {
        executor.execute(() -> {
            allianceRepository.getAllAllianceInvites(userId, new RepositoryCallback<List<AllianceInvite>>() {
                @Override
                public void onSuccess(List<AllianceInvite> data) {
                    invites.postValue(data);  // sada će uvek biti kompletan invite
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }


    public void acceptInvite(String inviteId, String userId) {
        executor.execute(() -> {
            userRepository.getUserById(userId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User currentUser) {
                    allianceRepository.getAllianceInviteById(userId, inviteId, new RepositoryCallback<AllianceInvite>() {
                        @Override
                        public void onSuccess(AllianceInvite invite) {
                            if (currentUser.getCurrentAlliance() != null && currentUser.getCurrentAlliance().isMissionActive()) {
                                error.postValue("Ne možete napustiti savez dok je misija aktivna");
                                return;
                            }

                            currentUser.setCurrentAlliance(null);
                            userRepository.updateUser(currentUser, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void result) {
                                    joinNewAlliance(invite, currentUser, userId);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    error.postValue(e.getMessage());
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            error.postValue(e.getMessage());
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }


    private void joinNewAlliance(AllianceInvite invite, User currentUser, String userId) {
        currentUser.setCurrentAlliance(invite.getAlliance());
        userRepository.updateUser(currentUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                allianceRepository.acceptInvite(userId, invite.getId(), new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        successMessage.postValue("Poziv prihvaćen! Pristupili ste savezu " + invite.getAlliance().getName());
                        loadInvites(userId);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.postValue(e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }


    public void rejectInvite(String inviteId, String userId) {
        successMessage.setValue(null);
        error.setValue(null);

        executor.execute(() -> {
            allianceRepository.rejectInvite(userId, inviteId, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    successMessage.postValue("Invite rejected");
                    loadInvites(userId);
                }

                @Override
                public void onFailure(Exception e) {
                    error.postValue(e.getMessage());
                }
            });
        });
    }

    public void getAllianceInviteById(String inviteId, String userId, RepositoryCallback<AllianceInvite> callback) {
        executor.execute(() -> {
            allianceRepository.getAllianceInviteById(userId, inviteId, new RepositoryCallback<AllianceInvite>() {
                @Override
                public void onSuccess(AllianceInvite invite) {
                    callback.onSuccess(invite);
                }

                @Override
                public void onFailure(Exception e) {
                    callback.onFailure(e);
                }
            });
        });
    }

    public void getFullInvite(String inviteId, String userId, RepositoryCallback<AllianceInvite> callback) {
        executor.execute(() -> {
            allianceRepository.getAllianceInviteById(userId, inviteId, new RepositoryCallback<AllianceInvite>() {
                @Override
                public void onSuccess(AllianceInvite invite) {
                    // Dohvati fromUser po ID-ju
                    userRepository.getUserById(invite.getFromUser().getUsername(), new RepositoryCallback<User>() {
                        @Override
                        public void onSuccess(User fromUser) {
                            invite.setFromUser(fromUser);

                            // Dohvati alliance po ID-ju
                            allianceRepository.getAllianceById(fromUser.getId(), invite.getAlliance().getId(), new RepositoryCallback<Alliance>() {
                                @Override
                                public void onSuccess(Alliance alliance) {
                                    invite.setAlliance(alliance);
                                    callback.onSuccess(invite);
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
        });
    }



    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceRepository repository;
        private final Executor executor;
        private final UserRepository userRepository;

        public Factory(AllianceRepository repository, Executor executor, UserRepository userRepository) {
            this.repository = repository;
            this.executor = executor;
            this.userRepository = userRepository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceInvitationViewModel.class)) {
                return (T) new AllianceInvitationViewModel(repository, executor, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
