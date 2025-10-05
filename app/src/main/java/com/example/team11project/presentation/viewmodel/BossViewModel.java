package com.example.team11project.presentation.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.BossBattleRepositoryImpl;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.BossRewardRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.ChlothingEffectType;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.SpecialTaskType;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.AllianceMissionUseCase;
import com.example.team11project.domain.usecase.BossUseCase;

import java.util.Iterator;
import java.util.List;

public class BossViewModel extends ViewModel {

    private final BossBattleRepository bossBattleRepository;
    private final BossRepository bossRepository;

    private final EquipmentRepository equipmentRepository;
    private final UserRepository userRepository;
    private final BossUseCase bossUseCase;
    private User user;

    private final AllianceMissionUseCase allianceMissionUseCase;

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

    public BossViewModel(BossBattleRepository bossBattleRepository, BossRepository bossRepository, BossRewardRepository bossRewardRepository, EquipmentRepository equipmentRepository, AllianceMissionUseCase allianceMissionUseCase, UserRepository userRepository) {
        this.bossBattleRepository = bossBattleRepository;
        this.bossRepository = bossRepository;
        this.equipmentRepository = equipmentRepository;
        this.allianceMissionUseCase = allianceMissionUseCase;
        this.userRepository = userRepository;
        this.bossUseCase = new BossUseCase(bossRepository, bossBattleRepository, bossRewardRepository, equipmentRepository, userRepository);
    }

    public void loadBattleWithBoss(String userId, String bossId, int level, User currentUser) {
        if (bossId == null) {
            _error.setValue("Nevalidna bitka ili bossId");
            return;
        }

        user = currentUser;

        _isLoading.setValue(true);
        _error.setValue(null);

        bossBattleRepository.getBattleByUserAndBossAndLevel(userId, bossId, level, new RepositoryCallback<BossBattle>() {
            @Override
            public void onSuccess(BossBattle battle) {
                if (battle == null) {
                    _error.setValue("Trenutno nema aktivne borbe");
                    _isLoading.postValue(false);
                    return;
                }

                if (currentUser != null) {
                    List<String> activeIds = battle.getActiveEquipment();
                    int temporaryPP = 0;

                    for (Potion p : currentUser.getPotions()) {
                        if (activeIds.contains(p.getId()) && !p.isPermanent())
                            temporaryPP += p.getPowerBoostPercent();
                    }

                    for (Clothing c : currentUser.getClothing()) {
                        if (activeIds.contains(c.getId())) {
                            switch(c.getEffectType()) {
                                case STRENGTH:
                                    currentUser.getLevelInfo().setPp(currentUser.getLevelInfo().getPp() + c.getEffectPercent());
                                    break;
                                case SUCCESS_RATE:
                                    battle.setHitChance(battle.getHitChance() + 10); // privremeni bonus +10%
                                    break;
                                case EXTRA_ATTACKS:
                                    int extraAttacks = (int)(5 * 0.4); // 40% više napada
                                    battle.setAttacksUsed(battle.getAttacksUsed() + extraAttacks);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }


                    battle.setUserPP(battle.getUserPP() + temporaryPP);
                }

                _bossBattle.postValue(battle);

                bossRepository.getBossById(userId, bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss bossObj) {
                        _boss.postValue(bossObj);
                        _isLoading.postValue(false);

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


    public void performAttack(String userId) {
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
                    allianceMissionUseCase.processSpecialTask(userId, SpecialTaskType.REGULAR_BOSS_HIT, new RepositoryCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean damageDealt) {
                            if (damageDealt) {
                                Log.d("SpecialMission", "Store purchase registered!");
                            }
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("SpecialMission", "Failed to process task: " + e.getMessage());
                        }
                    });
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

                    if (bossUseCase.isBattleFinished(currentBattle)) {
                        _battleFinished.postValue(true);

                        if (user != null) {
                            Iterator<Clothing> clothingIterator = user.getClothing().iterator();
                            while (clothingIterator.hasNext()) {
                                Clothing c = clothingIterator.next();
                                if (c.isActive()) {
                                    c.setRemainingBattles(c.getRemainingBattles() - 1);
                                    if (c.getRemainingBattles() <= 0) {
                                        c.setActive(false);
                                        c.setQuantity(c.getQuantity() - 1);
                                    }
                                }
                                if (c.getQuantity() <= 0) {
                                    clothingIterator.remove();
                                }
                            }

                            Iterator<Potion> potionIterator = user.getPotions().iterator();
                            while (potionIterator.hasNext()) {
                                Potion p = potionIterator.next();
                                if (p.isActive()) {
                                    p.setActive(false);
                                    p.setQuantity(p.getQuantity() - 1);
                                }
                                if (p.getQuantity() <= 0) {
                                    potionIterator.remove();
                                }
                            }

                            Iterator<Weapon> weaponIterator = user.getWeapons().iterator();
                            while (weaponIterator.hasNext()) {
                                Weapon w = weaponIterator.next();
                                if (w.isActive()) {
                                    w.setActive(false);
                                     w.setQuantity(w.getQuantity() - 1);
                                }
                                if (w.getQuantity() <= 0) {
                                    weaponIterator.remove();
                                }
                            }

                            userRepository.updateUser(user, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    Log.d("BossVM", "Oprema resetovana i očišćena");
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    Log.e("BossVM", "Greška pri resetovanju/opremi: " + e.getMessage());
                                }
                            });
                        }

                    }


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
                    AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(application);
                    AllianceRepository allianceRepository = new AllianceRepositoryImpl(application);
                    UserRepository userRepository = new UserRepositoryImpl(application);
                    TaskRepository taskRepository = new TaskRepositoryImpl(application);
                    TaskInstanceRepository taskInstanceRepository = new TaskInstanceRepositoryImpl(application);
                    AllianceMissionUseCase allianceMissionUseCase1 = new AllianceMissionUseCase(allianceMissionRepository, allianceRepository, userRepository, taskRepository, taskInstanceRepository);


                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    BossBattleRepository.class,
                                    BossRepository.class,
                                    BossRewardRepository.class,
                                    EquipmentRepository.class,
                                    AllianceMissionUseCase.class,
                                    UserRepository.class)
                            .newInstance(battleRepo, bossRepo, rewardRepo, equipmentRepo, allianceMissionUseCase1, userRepository);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
