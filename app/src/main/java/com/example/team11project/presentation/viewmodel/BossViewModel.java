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
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.usecase.BossUseCase;

public class BossViewModel extends ViewModel {

    private final BossBattleRepository bossBattleRepository;
    private final BossRepository bossRepository;

    private final EquipmentRepository equipmentRepository;
    private final BossUseCase bossUseCase;

    private final MutableLiveData<BossBattle> _bossBattle = new MutableLiveData<>();
    public final LiveData<BossBattle> bossBattle = _bossBattle;
    private final MutableLiveData<Boss> _boss = new MutableLiveData<>();
    public final LiveData<Boss> boss = _boss;


    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<String> _attackResult = new MutableLiveData<>();
    public final LiveData<String> attackResult = _attackResult;

    private final MutableLiveData<Boolean> _battleFinished = new MutableLiveData<>();
    public final LiveData<Boolean> battleFinished = _battleFinished;

    public BossViewModel(BossBattleRepository bossBattleRepository, BossRepository bossRepository, BossRewardRepository bossRewardRepository, EquipmentRepository equipmentRepository) {
        this.bossBattleRepository = bossBattleRepository;
        this.bossRepository = bossRepository;
        this.equipmentRepository = equipmentRepository;
        this.bossUseCase = new BossUseCase(bossRepository, bossBattleRepository, bossRewardRepository, equipmentRepository);
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

                        // Provjeri da li je borba već završena
                        if (bossUseCase.isBattleFinished(battle)) {
                            _battleFinished.postValue(true);
                        }
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

    public void performAttack() {
        BossBattle currentBattle = _bossBattle.getValue();
        Boss currentBoss = _boss.getValue();

        if (currentBattle == null || currentBoss == null) {
            _error.setValue("Nema aktivne borbe");
            return;
        }

        if (bossUseCase.isBattleFinished(currentBattle)) {
            _error.setValue("Borba je već završena");
            return;
        }

        _isLoading.setValue(true);

        bossUseCase.performAttack(currentBattle, currentBoss, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean isHit) {
                // Ažuriraj LiveData sa novim stanjem
                _bossBattle.postValue(currentBattle);
                _boss.postValue(currentBoss);

                // Postavi rezultat napada
                String result;
                if (isHit) {
                    result = "Pogodak! Nanešena šteta: " + currentBattle.getUserPP();
                    if (currentBoss.getCurrentHP() <= 0) {
                        result += "\nBoss je poražen!";
                    }
                } else {
                    result = "Promašaj!";
                }
                _attackResult.postValue(result);

                // Provjeri da li je borba završena
                if (bossUseCase.isBattleFinished(currentBattle)) {
                    _battleFinished.postValue(true);

                    // Dodaj informaciju o završetku borbe
                    if (currentBattle.isBossDefeated()) {
                        _attackResult.postValue(result + "\n\nBorba završena - POBEDA!");
                    } else {
                        double damagePercent = (double) currentBattle.getDamageDealt() / currentBoss.getMaxHP();
                        if (damagePercent >= 0.5) {
                            _attackResult.postValue(result + "\n\nBorba završena - Delimična pobeda!");
                        } else {
                            _attackResult.postValue(result + "\n\nBorba završena - Poraz!");
                        }
                    }
                }

                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greška pri napadu: " + e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    public void clearAttackResult() {
        _attackResult.setValue(null);
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
                    BossRewardRepository rewardRepo = new BossRewardRepositoryImpl(application);
                    EquipmentRepository equipmentRepo = new EquipmentRepositoryImpl(application);

                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    BossBattleRepository.class,
                                    BossRepository.class,
                                    BossRewardRepository.class,
                                    EquipmentRepository.class)
                            .newInstance(battleRepo, bossRepo, rewardRepo, equipmentRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
