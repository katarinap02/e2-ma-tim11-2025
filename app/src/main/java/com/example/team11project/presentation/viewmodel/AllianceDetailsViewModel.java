package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.AllianceMissionUseCase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AllianceDetailsViewModel extends ViewModel {

    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;

    private final AllianceMissionRepository allianceMissionRepository;

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<String> missionButtonText = new MutableLiveData<>();

    public AllianceDetailsViewModel(AllianceRepository allianceRepository, UserRepository userRepository, AllianceMissionRepository allianceMissionRepository) {
        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
        this.allianceMissionRepository = allianceMissionRepository;
    }

    public LiveData<Alliance> getAlliance() { return allianceLiveData; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getMissionButtonText() { return missionButtonText; }

    public void loadAlliance(String userId, String allianceId) {
        allianceRepository.getAllianceById(userId, allianceId, new RepositoryCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance result) {
                allianceLiveData.postValue(result);
                Log.d("AllianceRepository", "Loaded alliance: " + result);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue(e.getMessage());
                Log.d("AllianceRepository", "Nije uspeo da vrati alliance");

            }
        });
    }

    public void disbandAlliance(Alliance alliance) {
        if (alliance == null) {
            errorMessage.postValue("Alliance is null");
            return;
        }

        if (alliance.isMissionActive()) {
            errorMessage.postValue("Cannot disband alliance while mission is active");
            return;
        }

        allianceRepository.deleteAlliance(alliance.getId(), alliance.getLeader(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<String> members = alliance.getMembers();
                if (members == null || members.isEmpty()) {
                    allianceLiveData.postValue(null);
                    return;
                }

                AtomicInteger counter = new AtomicInteger(0);
                for (String memberId : members) {
                    userRepository.getUserById(memberId, new RepositoryCallback<com.example.team11project.domain.model.User>() {
                        @Override
                        public void onSuccess(com.example.team11project.domain.model.User user) {
                            if (user != null) {
                                user.setCurrentAlliance(null);
                                userRepository.updateUser(user, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void result) {
                                        if (counter.incrementAndGet() == members.size()) {
                                            // svi članovi su update-ovani
                                            allianceLiveData.postValue(null);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("AllianceDetailsVM", "Failed to update member " + user.getId(), e);
                                        if (counter.incrementAndGet() == members.size()) {
                                            allianceLiveData.postValue(null);
                                        }
                                    }
                                });
                            } else {
                                if (counter.incrementAndGet() == members.size()) {
                                    allianceLiveData.postValue(null);
                                }
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("AllianceDetailsVM", "Failed to fetch member " + memberId, e);
                            if (counter.incrementAndGet() == members.size()) {
                                allianceLiveData.postValue(null);
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Failed to disband alliance: " + e.getMessage());
            }
        });
    }

    //------------------DEO SA MISIJOM--------------------//
    public void checkActiveMission(String allianceId, String currentUserId, String leaderId) {
        allianceMissionRepository.getActiveMissionByAllianceId(allianceId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                if (mission != null) {
                    // postoji aktivna misija
                    missionButtonText.postValue("Napredak misije");
                } else {
                    // nema aktivne misije, samo lider može da započne
                    if (currentUserId.equals(leaderId)) {
                        missionButtonText.postValue("Zapocni misiju");
                    } else {
                        missionButtonText.postValue(""); // sakrij dugme ili onemogući
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Neuspešno učitavanje misije: " + e.getMessage());
            }
        });
    }

    public void startSpecialMission(Alliance alliance) {
        if (alliance == null) {
            errorMessage.postValue("Alliance is null");
            return;
        }

        AllianceMissionUseCase allianceMissionUseCase = new AllianceMissionUseCase(allianceMissionRepository, allianceRepository);

        allianceMissionUseCase.startSpecialMission(alliance, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                // ažuriraj alliance da označiš da je misija aktivna
                alliance.setMissionActive(true);
                allianceLiveData.postValue(alliance);

                Log.d("AllianceDetailsVM", "Mission started successfully: " + mission);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Failed to start mission: " + e.getMessage());
            }
        });
    }





    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceRepository allianceRepository;
        private final UserRepository userRepository;

        private final AllianceMissionRepository allianceMissionRepository;

        public Factory(AllianceRepository allianceRepository, UserRepository userRepository, AllianceMissionRepository allianceMissionRepository) {
            this.allianceRepository = allianceRepository;
            this.userRepository = userRepository;
            this.allianceMissionRepository = allianceMissionRepository;

        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceDetailsViewModel.class)) {
                return (T) new AllianceDetailsViewModel(allianceRepository, userRepository, allianceMissionRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
