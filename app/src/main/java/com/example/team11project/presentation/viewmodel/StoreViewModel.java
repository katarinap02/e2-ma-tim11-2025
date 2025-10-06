package com.example.team11project.presentation.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.data.repository.AllianceMissionRepositoryImpl;
import com.example.team11project.data.repository.AllianceRepositoryImpl;
import com.example.team11project.data.repository.BossRepositoryImpl;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.ChlothingEffectType;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.SpecialTaskType;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.AllianceRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.AllianceMissionUseCase;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StoreViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final BossRepository bossRepository;
    private final AllianceMissionUseCase allianceMissionUseCase;

    private final MutableLiveData<List<Potion>> potions = new MutableLiveData<>();
    private final MutableLiveData<List<Weapon>> weapons = new MutableLiveData<>();
    private final MutableLiveData<List<Clothing>> clothing = new MutableLiveData<>();

    private final MutableLiveData<String> error = new MutableLiveData<>();


    public StoreViewModel(UserRepository repository, EquipmentRepository equipmentRepository, AllianceMissionUseCase allianceMissionUseCase, BossRepository bossRepository) {
        this.userRepository = repository;
        this.equipmentRepository = equipmentRepository;
        this.allianceMissionUseCase = allianceMissionUseCase;
        this.bossRepository = bossRepository;
    }

    // --- Potions ---
    public LiveData<List<Potion>> getPotions() {
        return potions;
    }

    // --- Weapons ---
    public LiveData<List<Weapon>> getWeapons() {
        return weapons;
    }

    // --- Clothing ---
    public LiveData<List<Clothing>> getClothing() {
        return clothing;
    }

    public void loadEquipment(String userId) {
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> data) {
                List<Clothing> clothingList = new ArrayList<>();
                List<Potion> potionsList = new ArrayList<>();

                for (Equipment e : data) {
                    if (e instanceof Clothing) clothingList.add((Clothing) e);
                    else if (e instanceof Potion) potionsList.add((Potion) e);
                }

                // Ako nema proizvoda, odmah update LiveData
                if (clothingList.isEmpty() && potionsList.isEmpty()) {
                    clothing.postValue(clothingList);
                    potions.postValue(potionsList);
                    return;
                }

                if (!clothingList.isEmpty()) {
                    for (int i = 0; i < clothingList.size(); i++) {
                        final int index = i;
                        Clothing c = clothingList.get(i);

                        userRepository.getUserById(userId, new RepositoryCallback<User>() {
                            @Override
                            public void onSuccess(User user) {
                                calculateAndSetPriceForClothing(user, c, new RepositoryCallback<Double>() {
                                    @Override
                                    public void onSuccess(Double calculatedPrice) {
                                        c.setPrice(calculatedPrice);
                                        if (index == clothingList.size() - 1) {
                                            clothing.postValue(clothingList);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("StoreViewModel", "Greška pri izračunavanju cene odeće: " + e.getMessage());
                                        if (index == clothingList.size() - 1) {
                                            clothing.postValue(clothingList);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("StoreViewModel", "Greška pri dobijanju usera: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    clothing.postValue(clothingList);
                }

                if (!potionsList.isEmpty()) {
                    for (int i = 0; i < potionsList.size(); i++) {
                        final int index = i;
                        Potion p = potionsList.get(i);

                        userRepository.getUserById(userId, new RepositoryCallback<User>() {
                            @Override
                            public void onSuccess(User user) {
                                calculateAndSetPriceForPotion(user, p, new RepositoryCallback<Double>() {
                                    @Override
                                    public void onSuccess(Double calculatedPrice) {
                                        p.setPrice(calculatedPrice);
                                        if (index == potionsList.size() - 1) {
                                            potions.postValue(potionsList);
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("StoreViewModel", "Greška pri izračunavanju cene napitka: " + e.getMessage());
                                        if (index == potionsList.size() - 1) {
                                            potions.postValue(potionsList);
                                        }
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                Log.e("StoreViewModel", "Greška pri dobijanju usera: " + e.getMessage());
                            }
                        });
                    }
                } else {
                    potions.postValue(potionsList);
                }
            }

            @Override
            public void onFailure(Exception e) {
                clothing.postValue(new ArrayList<>());
                potions.postValue(new ArrayList<>());
            }
        });
    }

    public LiveData<String> getError() {
        return error;
    }

    public void buyClothing(String userId, Clothing item) {
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                calculateAndSetPriceForClothing(user, item, new RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double calculatedPrice) {
                        if (user.getCoins() < calculatedPrice) {
                            error.postValue("Nemaš dovoljno novca");
                            return;
                        }

                        item.setPrice(calculatedPrice);

                        if (user.getClothing() == null) user.setClothing(new ArrayList<>());

                        Clothing existing = null;
                        for (Clothing c : user.getClothing()) {
                            if (c.getName().equals(item.getName())) {
                                existing = c;
                                break;
                            }
                        }

                        if (existing != null) {
                            existing.setQuantity(existing.getQuantity() + 1);
                        } else {
                            Clothing copy = new Clothing(UUID.randomUUID().toString(), item.getName(), item.getPrice(),
                                    item.getEffectPercent(), item.isActive(), 1, item.getEffectType(), item.getImage());
                            user.getClothing().add(copy);
                        }

                        user.setCoins((int) (user.getCoins() - calculatedPrice));

                        userRepository.updateUser(user, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                clothing.postValue(user.getClothing());
                                allianceMissionUseCase.processSpecialTask(userId, SpecialTaskType.STORE_PURCHASE, new RepositoryCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean damageDealt) {
                                        if (damageDealt) Log.d("SpecialMission", "Store purchase registered!");
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("SpecialMission", "Failed to process task: " + e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                error.postValue("Neuspešna kupovina: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.postValue("Greška pri izračunavanju cene: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Neuspešno učitavanje usera: " + e.getMessage());
            }
        });
    }
    public void buyPotion(String userId, Potion item) {
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                calculateAndSetPriceForPotion(user, item, new RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double calculatedPrice) {
                        item.setPrice(calculatedPrice);

                        if (user.getCoins() < calculatedPrice) {
                            error.postValue("Nemaš dovoljno novca");
                            return;
                        }

                        if (user.getPotions() == null) user.setPotions(new ArrayList<>());

                        Potion existing = null;
                        for (Potion p : user.getPotions()) {
                            if (p.getName().equals(item.getName())) {
                                existing = p;
                                break;
                            }
                        }

                        if (existing != null) {
                            existing.setQuantity(existing.getQuantity() + 1);
                        } else {
                            Potion copy = new Potion(UUID.randomUUID().toString(), item.getName(), item.getPrice(),
                                    item.getPowerBoostPercent(), item.isPermanent(), item.isActive(), 1, item.getImage());
                            user.getPotions().add(copy);
                        }

                        user.setCoins((int) (user.getCoins() - calculatedPrice));

                        userRepository.updateUser(user, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                potions.postValue(user.getPotions());

                                allianceMissionUseCase.processSpecialTask(userId, SpecialTaskType.STORE_PURCHASE, new RepositoryCallback<Boolean>() {
                                    @Override
                                    public void onSuccess(Boolean damageDealt) {
                                        if (damageDealt) Log.d("SpecialMission", "Store purchase registered!");
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        Log.e("SpecialMission", "Failed to process task: " + e.getMessage());
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                error.postValue("Neuspešna kupovina: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.postValue("Greška pri izračunavanju cene: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Neuspešno učitavanje usera: " + e.getMessage());
            }
        });
    }

    private void calculateAndSetPriceForPotion(User user, Potion potion, RepositoryCallback<Double> callback) {
        int previousLevel = user.getLevelInfo().getLevel() - 1;
        if (previousLevel <= 0) {
            callback.onSuccess(potion.getPrice());
            return;
        }

        bossRepository.getBossByUserIdAndLevel(user.getId(), previousLevel, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss previousBoss) {
                int prevCoins = previousBoss != null ? previousBoss.getCoinsReward() : 0;
                double price = 0;

                switch (potion.getPowerBoostPercent()) {
                    case 20: // jednokratni 20%
                        price = prevCoins * 0.5;
                        break;
                    case 40: // jednokratni 40%
                        price = prevCoins * 0.7;
                        break;
                    case 5: // trajni 5%
                        price = prevCoins * 2.0;
                        break;
                    case 10: // trajni 10%
                        price = prevCoins * 10.0;
                        break;
                    default:
                        price = potion.getPrice();
                }

                callback.onSuccess(price);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("StoreViewModel", "Greška pri dobijanju prethodnog bossa: " + e.getMessage(), e);
                callback.onSuccess(potion.getPrice());
            }
        });
    }

    private void calculateAndSetPriceForClothing(User user, Clothing clothing, RepositoryCallback<Double> callback) {
        int previousLevel = user.getLevelInfo().getLevel() - 1;
        if (previousLevel <= 0) {
            callback.onSuccess(clothing.getPrice());
            return;
        }

        bossRepository.getBossByUserIdAndLevel(user.getId(), previousLevel, new RepositoryCallback<Boss>() {
            @Override
            public void onSuccess(Boss previousBoss) {
                int prevCoins = previousBoss != null ? previousBoss.getCoinsReward() : 1;
                double price = 0;

                switch (clothing.getName()) {
                    case "Rukavice snage 10%":
                    case "Štit +10% šanse uspešnog napada":
                        price = prevCoins * 0.6;
                        break;
                    case "Čizme +40% šanse za dodatni napad":
                        price = prevCoins * 0.8;
                        break;
                    default:
                        price = clothing.getPrice();
                }

                callback.onSuccess(price);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("StoreViewModel", "Greška pri dobijanju prethodnog bossa: " + e.getMessage(), e);
                callback.onSuccess(clothing.getPrice());
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
            if (modelClass.isAssignableFrom(StoreViewModel.class)) {
                UserRepository repository = new UserRepositoryImpl(application);
                EquipmentRepository eRepository = new EquipmentRepositoryImpl(application);
                AllianceMissionRepository allianceMissionRepository = new AllianceMissionRepositoryImpl(application);
                AllianceRepository allianceRepository = new AllianceRepositoryImpl(application);
                TaskRepository taskRepository = new TaskRepositoryImpl(application);
                TaskInstanceRepository taskInstanceRepository = new TaskInstanceRepositoryImpl(application);
                EquipmentRepository equipmentRepository1 = new EquipmentRepositoryImpl(application);
                BossRepository bossRepository = new BossRepositoryImpl(application);
                AllianceMissionUseCase allianceUseCase = new AllianceMissionUseCase(allianceMissionRepository, allianceRepository, repository, taskRepository, taskInstanceRepository, equipmentRepository1, bossRepository);

                return (T) new StoreViewModel(repository, eRepository, allianceUseCase, bossRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

