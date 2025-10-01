package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.MutableLiveData;

import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;

import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

public class AllianceMissionViewModel extends ViewModel {
    private final AllianceMissionRepository allianceMissionRepository;

    public AllianceMissionViewModel(AllianceMissionRepository allianceMissionRepository)
    {
        this.allianceMissionRepository = allianceMissionRepository;
    }

    private final MutableLiveData<AllianceMission> activeMission = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

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
                activeMission.postValue(mission); // postValue jer može biti u background thread-u
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue("Greška pri dohvatanju aktivne misije: " + e.getMessage());
            }
        });
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


                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    AllianceMissionRepository.class)
                            .newInstance(allRepository);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
