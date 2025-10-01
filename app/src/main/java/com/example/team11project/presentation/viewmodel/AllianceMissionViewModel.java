package com.example.team11project.presentation.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;

import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;

import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.MemberProgress;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class AllianceMissionViewModel extends ViewModel {
    private final AllianceMissionRepository allianceMissionRepository;
    private final UserRepository userRepository;

    public AllianceMissionViewModel(AllianceMissionRepository allianceMissionRepository, UserRepository userRepository)
    {
        this.allianceMissionRepository = allianceMissionRepository;
        this.userRepository = userRepository;
    }

    private final MutableLiveData<AllianceMission> activeMission = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    private final Map<String, String> userMap = new HashMap<>();
    private final MutableLiveData<Map<String, String>> membersLiveData = new MutableLiveData<>();
    public LiveData<Map<String, String>> getMembersLiveData() {
        return membersLiveData;
    }

    public LiveData<AllianceMission> getActiveMission() {
        return activeMission;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void getActiveMissionByAllianceId(String allianceId) {
        allianceMissionRepository.getActiveMissionByAllianceId(allianceId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                activeMission.postValue(mission);
                loadUsernamesForMission(mission);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Greška pri dohvatanju aktivne misije: " + e.getMessage());
            }
        });
    }

    public void loadUsernamesForMission(AllianceMission mission) {
        if (mission == null || mission.getMemberProgressList() == null) return;

        for (MemberProgress progress : mission.getMemberProgressList()) {
            String userId = progress.getUserId();
            userRepository.getUserById(userId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    userMap.put(user.getId(), user.getUsername());
                    membersLiveData.postValue(new HashMap<>(userMap)); // osveži UI
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("AllianceMissionVM", "Greška za userId " + userId, e);
                }
            });
        }
    }





    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceMissionViewModel.class)) {
                try {
                    AllianceMissionRepository allRepository = new AllianceMissionRepositoryImpl(application);
                    UserRepository userRepo = new UserRepositoryImpl(application);

                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    AllianceMissionRepository.class,
                                    UserRepository.class)
                            .newInstance(allRepository, userRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
