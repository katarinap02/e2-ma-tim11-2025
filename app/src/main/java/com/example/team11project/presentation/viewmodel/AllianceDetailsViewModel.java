package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

public class AllianceDetailsViewModel extends ViewModel {

    private final AllianceRepository allianceRepository;
    private final UserRepository userRepository;

    private final MutableLiveData<Alliance> alliance = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AllianceDetailsViewModel(AllianceRepository allianceRepository, UserRepository userRepository) {
        this.allianceRepository = allianceRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Alliance> getAlliance() { return alliance; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void loadAlliance(String userId, String allianceId) {
        allianceRepository.getAllianceById(userId, allianceId, new RepositoryCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance result) {
                alliance.postValue(result);
                Log.d("AllianceRepository", "Loaded alliance: " + result);
            }

            @Override
            public void onFailure(Exception e) {
                errorMessage.postValue(e.getMessage());
                Log.d("AllianceRepository", "Nije uspeo da vrati alliance");

            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceRepository allianceRepository;
        private final UserRepository userRepository;

        public Factory(AllianceRepository allianceRepository, UserRepository userRepository) {
            this.allianceRepository = allianceRepository;
            this.userRepository = userRepository;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceDetailsViewModel.class)) {
                return (T) new AllianceDetailsViewModel(allianceRepository, userRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
