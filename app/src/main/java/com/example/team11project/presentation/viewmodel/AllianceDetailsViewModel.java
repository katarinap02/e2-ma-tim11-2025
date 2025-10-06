package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.MissionFinalizationResult;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.AllianceMissionUseCase;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AllianceDetailsViewModel extends ViewModel {

    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;

    private final AllianceMissionRepository allianceMissionRepository;

    private final TaskRepository taskRepository;

    private final TaskInstanceRepository taskInstanceRepository;

    private final EquipmentRepository equipmentRepository;

    private final BossRepository bossRepository;

    private final MutableLiveData<Alliance> allianceLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final MutableLiveData<String> missionButtonText = new MutableLiveData<>();

    private final MutableLiveData<String> missionStarted = new MutableLiveData<>();

    private MutableLiveData<String> successMessage = new MutableLiveData<>();

    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);


    public AllianceDetailsViewModel(AllianceRepository allianceRepository, UserRepository userRepository, AllianceMissionRepository allianceMissionRepository, TaskRepository taskRepository, TaskInstanceRepository taskInstanceRepository, EquipmentRepository equipmentRepository, BossRepository bossRepository) {
        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
        this.allianceMissionRepository = allianceMissionRepository;
        this.taskRepository = taskRepository;
        this.taskInstanceRepository = taskInstanceRepository;
        this.equipmentRepository = equipmentRepository;
        this.bossRepository = bossRepository;
    }

    public LiveData<Alliance> getAlliance() { return allianceLiveData; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<String> getMissionButtonText() { return missionButtonText; }
    public LiveData<String> getMissionStarted() {
        return missionStarted;
    }
    public LiveData<String> getSuccessMessage() {
        return successMessage;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

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
        isLoading.postValue(true);
        allianceMissionRepository.getActiveMissionByAllianceId(allianceId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                isLoading.postValue(false);
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
                isLoading.postValue(false);
                errorMessage.postValue("Neuspešno učitavanje misije: " + e.getMessage());
            }
        });
    }

    public void checkAndFinalizeExpiredMission(Alliance alliance, String userId) {
        if (alliance == null) {
            errorMessage.postValue("Alliance is null");
            return;
        }
        isLoading.postValue(true);

        AllianceMissionUseCase allianceMissionUseCase = new AllianceMissionUseCase(
                allianceMissionRepository, allianceRepository, userRepository, taskRepository, taskInstanceRepository, equipmentRepository, bossRepository
        );

        allianceMissionUseCase.checkAndFinalizeMission(alliance, new RepositoryCallback<MissionFinalizationResult>() {
            @Override
            public void onSuccess(MissionFinalizationResult result) {
                isLoading.postValue(false);
                if (result.isWasFinalized()) {
                    alliance.setMissionActive(false);
                    allianceLiveData.postValue(alliance);

                    if (result.isBossDefeated()) {
                        successMessage.postValue("Prethodna misija je završena. Pobedili ste bosa. Preuzmite svoje nagrade.");
                    } else {
                        successMessage.postValue("Prethodna misija je završena. Boss nije poražen.");
                    }
                }
            }


            @Override
            public void onFailure(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Greška pri proveri misije: " + e.getMessage());
            }
        });
    }

    public void startSpecialMission(Alliance alliance, String userId) {
        if (alliance == null) {
            errorMessage.postValue("Alliance is null");
            return;
        }
        isLoading.postValue(true);

        AllianceMissionUseCase allianceMissionUseCase = new AllianceMissionUseCase(
                allianceMissionRepository, allianceRepository, userRepository, taskRepository, taskInstanceRepository,equipmentRepository, bossRepository
        );

        allianceMissionUseCase.startSpecialMission(alliance, userId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                isLoading.postValue(false);
                if (mission != null) {
                    alliance.setMissionActive(true);
                    allianceLiveData.postValue(alliance);
                    missionStarted.postValue(alliance.getId());
                    Log.d("AllianceDetailsVM", "Mission started successfully: " + mission);
                }
            }

            @Override
            public void onFailure(Exception e) {
                isLoading.postValue(false);
                errorMessage.postValue("Failed to start mission: " + e.getMessage());
            }
        });
    }

    public void refreshAllianceState(String allianceId, String currentUserId, String leaderId) {
        allianceRepository.getAllianceById(currentUserId, allianceId, new RepositoryCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance result) {
                allianceLiveData.postValue(result);
                checkActiveMission(allianceId, currentUserId, leaderId);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Greška pri osvežavanju: " + e.getMessage());
            }
        });
    }





    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceRepository allianceRepository;
        private final UserRepository userRepository;

        private final AllianceMissionRepository allianceMissionRepository;

        private final TaskRepository taskRepository;

        private final TaskInstanceRepository taskInstanceRepository;
        private final EquipmentRepository equipmentRepository;

        private final BossRepository bossRepository;

        public Factory(AllianceRepository allianceRepository, UserRepository userRepository, AllianceMissionRepository allianceMissionRepository, TaskRepository taskRepository, TaskInstanceRepository taskInstanceRepository, EquipmentRepository equipmentRepository, BossRepository bossRepository) {
            this.allianceRepository = allianceRepository;
            this.userRepository = userRepository;
            this.allianceMissionRepository = allianceMissionRepository;
            this.taskRepository = taskRepository;
            this.taskInstanceRepository = taskInstanceRepository;
            this.equipmentRepository = equipmentRepository;
            this.bossRepository = bossRepository;

        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceDetailsViewModel.class)) {
                return (T) new AllianceDetailsViewModel(allianceRepository, userRepository, allianceMissionRepository, taskRepository, taskInstanceRepository, equipmentRepository, bossRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
