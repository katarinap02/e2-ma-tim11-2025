package com.example.team11project.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.CreateAllianceUseCase;

import java.util.List;

public class CreateAllianceViewModel extends ViewModel {

    private final CreateAllianceUseCase createAllianceUseCase;
    private final MutableLiveData<Alliance> createdAlliance = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // ovde sada primaš repozitorijume
    public CreateAllianceViewModel(AllianceRepository allianceRepository, UserRepository userRepository) {
        this.createAllianceUseCase = new CreateAllianceUseCase(allianceRepository, userRepository);
    }

    public LiveData<Alliance> getCreatedAlliance() {
        return createdAlliance;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void createAlliance(String leaderId, String allianceName, List<String> invitedUserIds) {
        createAllianceUseCase.createAlliance(
                leaderId, allianceName, invitedUserIds,
                new RepositoryCallback<Alliance>() {
                    @Override
                    public void onSuccess(Alliance alliance) {
                        createdAlliance.postValue(alliance);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        errorMessage.postValue(e.getMessage());
                    }
                }
        );
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final AllianceRepository allianceRepository;
        private final UserRepository userRepository;

        public Factory(AllianceRepository allianceRepository, UserRepository userRepository) {
            this.allianceRepository = allianceRepository;
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        @SuppressWarnings("unchecked")
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(CreateAllianceViewModel.class)) {
                return (T) new CreateAllianceViewModel(allianceRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }

    }
}
