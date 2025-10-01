package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.BossRewardRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.BossReward;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RewardViewModel extends ViewModel{

    private final EquipmentRepository equipmentRepository;

    private  final UserRepository userRepository;

    private final BossRewardRepository bossRewardRepository;

    private final BossRepository bossRepository;

    private final MutableLiveData<BossReward> _reward = new MutableLiveData<>();
    public final LiveData<BossReward> reward = _reward;

    private final MutableLiveData<Boolean> _rewardClaimed = new MutableLiveData<>();
    public final LiveData<Boolean> rewardClaimed = _rewardClaimed;

    private final MutableLiveData<String> _successMessage = new MutableLiveData<>();
    public final LiveData<String> successMessage = _successMessage;

    private final MutableLiveData<Equipment> _equipment = new MutableLiveData<>();
    public final LiveData<Equipment> equipment = _equipment;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    public RewardViewModel(EquipmentRepository equipmentRepository, UserRepository userRepository, BossRewardRepository bossRewardRepository, BossRepository bossRepository)
    {
        this.equipmentRepository = equipmentRepository;
        this.userRepository = userRepository;
        this.bossRewardRepository = bossRewardRepository;
        this.bossRepository = bossRepository;
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

    public void claimReward(String userId, String bossId, int level) {
        BossReward currentReward = _reward.getValue();
        if (currentReward == null) {
            _error.setValue("Nema dostupne nagrade");
            return;
        }

        _isLoading.setValue(true);
        _error.setValue(null);

        bossRepository.getBossByUserIdAndLevel(userId, level, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss boss) {
                if (boss != null) {
                    // Postavi XP na maksimum
                    if (!boss.isDefeated())
                    {
                        boss.setCurrentHP(boss.getMaxHP());
                    }
                    // Update bossa
                    bossRepository.updateBoss(boss, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // Kad se boss ažurira → ažuriramo korisnika
                            userRepository.getUserById(userId, new RepositoryCallback<User>() {
                                @Override
                                public void onSuccess(User user) {
                                    // Dodaj novac
                                    int newCoins = user.getCoins() + currentReward.getCoinsEarned();
                                    user.setCoins(newCoins);

                                    // Dodaj opremu ako postoji
                                    if (currentReward.getEquipmentId() != null && !currentReward.getEquipmentId().isEmpty()) {
                                        Equipment currentEquipment = _equipment.getValue();
                                        if (currentEquipment != null) {
                                            addEquipmentToUser(user, currentEquipment);
                                        }
                                    }

                                    // Ažuriraj korisnika
                                    userRepository.updateUser(user, new RepositoryCallback<Void>() {
                                        @Override
                                        public void onSuccess(Void data) {
                                            // Označi nagradu kao preuzetu
                                            markRewardAsClaimed(userId, bossId, level);
                                        }

                                        @Override
                                        public void onFailure(Exception e) {
                                            _error.postValue("Greška pri ažuriranju korisnika: " + e.getMessage());
                                            _isLoading.postValue(false);
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    _error.postValue("Greška pri učitavanju korisnika: " + e.getMessage());
                                    _isLoading.postValue(false);
                                }
                            });
                        }

                        @Override
                        public void onFailure(Exception e) {
                            _error.postValue("Greška pri ažuriranju bossa: " + e.getMessage());
                            _isLoading.postValue(false);
                        }
                    });
                } else {
                    _error.postValue("Boss nije pronađen");
                    _isLoading.postValue(false);
                }
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greška pri učitavanju bossa: " + e.getMessage());
                _isLoading.postValue(false);
            }
        });
    }

    private void addEquipmentToUser(User user, Equipment equipment) {
        if (equipment instanceof Weapon) {
            addWeaponToUser(user, (Weapon) equipment);
        } else if (equipment instanceof Clothing) {
            addClothingToUser(user, (Clothing) equipment);
        } else {
            _error.postValue("Nepoznat tip opreme");
        }
    }

    private void addWeaponToUser(User user, Weapon weapon) {
        if (user.getWeapons() == null) {
            user.setWeapons(new ArrayList<>());
        }

        Weapon existing = null;
        for (Weapon w : user.getWeapons()) {
            if (w.getName().equals(weapon.getName())) {
                existing = w;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            Weapon copy = new Weapon(
                    UUID.randomUUID().toString(),
                    weapon.getName(),
                    weapon.getPrice(),
                    weapon.getPermanentBoostPercent(),
                    weapon.getUpgradeChance(),
                    weapon.isActive(),
                    1,
                    weapon.getEffectType(),
                    weapon.getImage()
            );
            user.getWeapons().add(copy);
        }

    }

    private void addClothingToUser(User user, Clothing clothing) {
        if (user.getClothing() == null) {
            user.setClothing(new ArrayList<>());
        }

        Clothing existing = null;
        for (Clothing c : user.getClothing()) {
            if (c.getName().equals(clothing.getName())) {
                existing = c;
                break;
            }
        }

        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + 1);
        } else {
            Clothing copy = new Clothing(
                    UUID.randomUUID().toString(),
                    clothing.getName(),
                    clothing.getPrice(),
                    clothing.getEffectPercent(),
                    clothing.isActive(),
                    1,
                    clothing.getEffectType(),
                    clothing.getImage()
            );
            user.getClothing().add(copy);

        }
    }

    private void markRewardAsClaimed(String userId, String bossId, int level) {
        BossReward currentReward = _reward.getValue();
        if (currentReward == null) {
            _isLoading.postValue(false);
            return;
        }

        // Resetuj coins na 0 i ukloni equipment iz nagrade
        currentReward.setCoinsEarned(0);
        currentReward.setEquipmentId(null);

        bossRewardRepository.updateReward(currentReward, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _rewardClaimed.postValue(true);
                _successMessage.postValue("Nagrada je uspešno preuzeta!");
                _isLoading.postValue(false);

                // Resetuj current reward i equipment
                _reward.postValue(currentReward);
                _equipment.postValue(null);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greška pri ažuriranju nagrade: " + e.getMessage());
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
                    BossRepository bossRepo = new BossRepositoryImpl(application);

                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    EquipmentRepository.class,
                                    UserRepository.class,
                                    BossRewardRepository.class,
                                    BossRepository.class)
                            .newInstance(equipmentRepo, userRepo, rewardRepo, bossRepo);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }



}
