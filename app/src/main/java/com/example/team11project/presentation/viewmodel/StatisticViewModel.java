package com.example.team11project.presentation.viewmodel;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.StatisticRepository;
import com.example.team11project.domain.repository.UserRepository;

import java.util.HashMap;
import java.util.Map;

public class StatisticViewModel extends ViewModel{

    private StatisticRepository repository;
    private MutableLiveData<Map<String, Integer>> tasksSummary = new MutableLiveData<>();
    public LiveData<Map<String, Integer>> getTasksSummary() { return tasksSummary; }
    private MutableLiveData<String> error = new MutableLiveData<>();

    private MutableLiveData<Map<String, Integer>> completedTasksPerCategory = new MutableLiveData<>();

    private MutableLiveData<Float> averageTaskDifficulty = new MutableLiveData<>();
    public LiveData<Float> getAverageTaskDifficulty() { return averageTaskDifficulty; }

    private final MutableLiveData<Integer> longestStreak = new MutableLiveData<>();
    public LiveData<Integer> getLongestStreak() {return longestStreak;}

    private final MutableLiveData<Map<String, Integer>> allianceMissionsSummary = new MutableLiveData<>();
    public LiveData<Map<String, Integer>> getAllianceMissionsSummary() { return allianceMissionsSummary; }

    public StatisticViewModel(StatisticRepository statisticRepository){
        this.repository = statisticRepository;
    }
    public void loadTasksSummary(String userId) {
        repository.getTasksSummary(userId, new RepositoryCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> result) {
                tasksSummary.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<Map<String, Integer>> getCompletedTasksPerCategory() {
        return completedTasksPerCategory;
    }

    public void loadCompletedTasksPerCategory(String userId) {
        repository.getCompletedTasksPerCategory(userId, new RepositoryCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> result) {
                completedTasksPerCategory.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }


    public void loadAverageTaskDifficulty(String userId) {
        repository.getAverageTaskDifficulty(userId, new RepositoryCallback<Float>() {
            @Override
            public void onSuccess(Float result) {
                averageTaskDifficulty.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void loadLongestStreak(String userId) {
        repository.getLongestSuccessStreak(userId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer result) {
                longestStreak.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public LiveData<String> getError() {
        return error;
    }

    public void loadAllianceMissionsSummary(String userId) {
        repository.getUserAllianceMissionsSummary(userId, new RepositoryCallback<Map<String, Integer>>() {
            @Override
            public void onSuccess(Map<String, Integer> result) {
                allianceMissionsSummary.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final StatisticRepository statisticRepository;

        public Factory(StatisticRepository statisticRepository) {
            this.statisticRepository = statisticRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(StatisticViewModel.class)) {
                return (T) new StatisticViewModel(statisticRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
