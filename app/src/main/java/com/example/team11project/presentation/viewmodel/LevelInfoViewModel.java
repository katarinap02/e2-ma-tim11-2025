package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.LevelInfo;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.ResourceBundle;

public class LevelInfoViewModel extends ViewModel {

    private final LevelInfoRepository repository;

    private final MutableLiveData<Integer> _progress = new MutableLiveData<>();

    private final MutableLiveData<LevelInfo> _levelInfo = new MutableLiveData<>();

    private User currentUser;

    public LevelInfoViewModel(LevelInfoRepository repository, String userId) {
        this.repository = repository;
        loadUser(userId);
    }

    private void loadUser(String userId) {
        try {
            repository.getUserById(userId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    currentUser = user;
                    updateLiveDataFromUser();
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("LevelInfoVM", "Failed to load user", e);
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            Log.e("LevelInfoVM", "Exception in loadUser", e);
        }
    }


    public LiveData<Integer> getProgress() { return _progress; }
    public LiveData<LevelInfo> getLevelInfo() { return _levelInfo; }

    public void addXp(int amount) {
        if (currentUser == null) return;

        repository.addXp(currentUser, amount, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateLiveDataFromUser();
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void updateLiveDataFromUser() {
        Log.d("LevelInfoVM", "Updating LiveData from user: " + currentUser);

        if (currentUser == null || currentUser.getLevelInfo() == null) return;

        LevelInfo li = currentUser.getLevelInfo();
        _levelInfo.postValue(li);


        int progressPercent = li.getXpForNextLevel() > 0
                ? (int) (((double) li.getXp() / li.getXpForNextLevel()) * 100)
                : 0;
        _progress.postValue(progressPercent);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final LevelInfoRepository repository;
        private final String userId;

        public Factory(LevelInfoRepository repository, String userId) {
            this.repository = repository;
            this.userId = userId;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(LevelInfoViewModel.class)) {
                return (T) new LevelInfoViewModel(repository, userId);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
