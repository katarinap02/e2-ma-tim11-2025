package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.data.repository.BossRewardRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossReward;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.List;

public class RewardViewModel extends ViewModel{

    private final EquipmentRepository equipmentRepository;

    private  final UserRepository userRepository;

    private final BossRewardRepository bossRewardRepository;

    private final MutableLiveData<BossReward> _reward = new MutableLiveData<>();
    public final LiveData<BossReward> reward = _reward;

    private final MutableLiveData<Equipment> _equipment = new MutableLiveData<>();
    public final LiveData<Equipment> equipment = _equipment;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public RewardViewModel(EquipmentRepository equipmentRepository, UserRepository userRepository, BossRewardRepository bossRewardRepository)
    {
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.bossRewardRepository = bossRewardRepository;
    }

    public void fetchReward(String userId, String bossId, int level) {
        if (bossId == null) {
            _error.setValue("Nevalidan user ili bossId");
            return;
        }

        _isLoading.setValue(true);
        _error.setValue(null);

        bossRewardRepository.getRewardByUserAndBossAndLevel(userId, bossId, level, new RepositoryCallback<BossReward>() {
            @Override
            public void onSuccess(BossReward reward) {
                _reward.postValue(reward);
                if (reward.getEquipmentId() != null && !reward.getEquipmentId().isEmpty()) {
                    fetchEquipmentById(reward.getEquipmentId());
                }
                else {
                    _isLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greška pri učitavanju nagrade: " + e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    private void fetchEquipmentById(String equipmentId) {
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> equipmentList) {
                Equipment foundEquipment = null;

                // Pronađi equipment čiji je ID jednak equipmentId
                for (Equipment equipment : equipmentList) {
                    if (equipmentId.equals(equipment.getImage())) {
                        foundEquipment = equipment;
                        break;
                    }
                }

                if (foundEquipment != null) {
                    _equipment.postValue(foundEquipment);
                } else {
                    _error.postValue("Equipment sa ID " + equipmentId + " nije pronađen");
                }

                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greška pri učitavanju equipment: " + e.getMessage());
                _isLoading.postValue(false);
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
            if (modelClass.isAssignableFrom(RewardViewModel.class)) {
                try {
                    EquipmentRepository equipmentRepo = new EquipmentRepositoryImpl(application);
                    UserRepository userRepo = new UserRepositoryImpl(application);
                    BossRewardRepository rewardRepo = new BossRewardRepositoryImpl(application);

                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    EquipmentRepository.class,
                                    UserRepository.class,
                                    BossRewardRepository.class)
                            .newInstance(equipmentRepo, userRepo, rewardRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }



}
