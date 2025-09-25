package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.BossBattleRepositoryImpl;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.BossRewardRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.RepositoryCallback;

public class BossViewModel extends ViewModel {

    private final BossBattleRepository bossBattleRepository;
    private final BossRepository bossRepository;

    private final MutableLiveData<BossBattle> _bossBattle = new MutableLiveData<>();
    public final LiveData<BossBattle> bossBattle = _bossBattle;
    private final MutableLiveData<Boss> _boss = new MutableLiveData<>();
    public final LiveData<Boss> boss = _boss;


    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public BossViewModel(BossBattleRepository bossBattleRepository, BossRepository bossRepository) {
        this.bossBattleRepository = bossBattleRepository;
        this.bossRepository = bossRepository;
    }

    public void loadBattleWithBoss(String userId, String bossId, int level) {
        if (bossId == null) {
            _error.setValue("Nevalidna bitka ili bossId");
            return;
        }

        _isLoading.setValue(true);
        _error.setValue(null);

        // Prvo učitaj BossBattle
        bossBattleRepository.getBattleByUserAndBossAndLevel(userId, bossId, level, new RepositoryCallback<BossBattle>() {
            @Override
            public void onSuccess(BossBattle battle) {
                _bossBattle.postValue(battle);

                // Kada je BossBattle učitan, učitaj Boss
                bossRepository.getBossById(userId, bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss bossObj) {
                        _boss.postValue(bossObj);
                        _isLoading.postValue(false);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        _error.postValue("Greška pri učitavanju bossa: " + e.getMessage());
                        _isLoading.postValue(false);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                _error.setValue("Greška pri učitavanju bitke: " + e.getMessage());
                _isLoading.setValue(false);
            }
        });
    }



    public void clearError() {
        _error.setValue(null);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;

        public Factory(Application application) {
            this.application = application;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(BossViewModel.class)) {
                try {
                    BossBattleRepository battleRepo = new BossBattleRepositoryImpl(application);
                    BossRepository bossRepo = new BossRepositoryImpl(application);

                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    BossBattleRepository.class,
                                    BossRepository.class)
                            .newInstance(battleRepo, bossRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
