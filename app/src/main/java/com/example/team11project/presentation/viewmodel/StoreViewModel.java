package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.ChlothingEffectType;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class StoreViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;

    private final MutableLiveData<List<Potion>> potions = new MutableLiveData<>();
    private final MutableLiveData<List<Weapon>> weapons = new MutableLiveData<>();
    private final MutableLiveData<List<Clothing>> clothing = new MutableLiveData<>();

    private final MutableLiveData<String> error = new MutableLiveData<>();


    public StoreViewModel(UserRepository repository, EquipmentRepository equipmentRepository) {
        this.userRepository = repository;
        this.equipmentRepository = equipmentRepository;
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

    public void loadEquipment() {
        equipmentRepository.getAllEquipment(new RemoteDataSource.DataSourceCallback<List<Equipment>>() {
            @Override
            public void onSuccess(List<Equipment> data) {
                List<Clothing> clothingList = new ArrayList<>();
                List<Potion> potionsList = new ArrayList<>();

                for (Equipment e : data) {
                    if ("clothing".equalsIgnoreCase(e.getType().name()) && e instanceof Clothing) {
                        clothingList.add((Clothing) e);
                    } else if ("potion".equalsIgnoreCase(e.getType().name()) && e instanceof Potion) {
                        potionsList.add((Potion) e);
                    }
                }

                clothing.postValue(clothingList);
                potions.postValue(potionsList);
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
                if (user.getCoins() < item.getPrice()) {
                    error.postValue("Nemaš dovoljno novca");
                    return;
                }

                if (user.getClothing() == null) {
                    user.setClothing(new ArrayList<>());
                }

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
                            item.getEffectPercent(), item.isActive(), 1, item.getEffectType());
                    user.getClothing().add(copy);
                }

                // Oduzimanje novca
                user.setCoins((int) (user.getCoins() - item.getPrice()));

                userRepository.updateUser(user, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        clothing.postValue(user.getClothing());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.postValue("Neuspešna kupovina: " + e.getMessage());
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
                if (user.getCoins() < item.getPrice()) {
                    error.postValue("Nemaš dovoljno novca");
                    return;
                }

                if (user.getPotions() == null) {
                    user.setPotions(new ArrayList<>());
                }

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
                            item.getPowerBoostPercent(), item.isPermanent(), item.isActive(), 1);
                    user.getPotions().add(copy);
                }

                // Oduzimanje novca
                user.setCoins((int) (user.getCoins() - item.getPrice()));

                Potion copy = new Potion(UUID.randomUUID().toString(), item.getName(), item.getPrice(),
                        item.getPowerBoostPercent(), item.isPermanent(), item.isActive(), item.getQuantity());
                user.getPotions().add(copy);

                userRepository.updateUser(user, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        potions.postValue(user.getPotions());
                    }

                    @Override
                    public void onFailure(Exception e) {
                        error.postValue("Neuspešna kupovina: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Neuspešno učitavanje usera: " + e.getMessage());
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

                return (T) new StoreViewModel(repository, eRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}

