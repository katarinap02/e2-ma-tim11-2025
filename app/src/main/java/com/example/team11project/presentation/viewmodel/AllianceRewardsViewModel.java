package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

import java.util.List;

public class AllianceRewardsViewModel extends ViewModel {

    private final AllianceMissionRepository allianceMissionRepository;

    private final MutableLiveData<List<AllianceMissionReward>> rewards = new MutableLiveData<>();
    private final MutableLiveData<Integer> totalBadges = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public AllianceRewardsViewModel(AllianceMissionRepository allianceMissionRepository) {
        this.allianceMissionRepository = allianceMissionRepository;
    }

    public LiveData<List<AllianceMissionReward>> getRewards() {
        return rewards;
    }

    public LiveData<Integer> getTotalBadges() {
        return totalBadges;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadRewards(String userId) {
        isLoading.setValue(true);
        error.setValue(null);

        allianceMissionRepository.getAllRewardsByUserId(userId, new RepositoryCallback<List<AllianceMissionReward>>() {
            @Override
            public void onSuccess(List<AllianceMissionReward> rewardList) {
                rewards.postValue(rewardList);

                // Izračunaj ukupan broj bedževa
                int total = 0;
                for (AllianceMissionReward reward : rewardList) {
                    total += reward.getBadgeCount();
                }
                totalBadges.postValue(total);

                isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Greška pri učitavanju nagrada: " + e.getMessage());
                isLoading.postValue(false);
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AllianceMissionRepository allianceMissionRepository;

        public Factory(AllianceMissionRepository allianceMissionRepository) {
            this.allianceMissionRepository = allianceMissionRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(AllianceRewardsViewModel.class)) {
                return (T) new AllianceRewardsViewModel(allianceMissionRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}