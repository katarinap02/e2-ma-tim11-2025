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
import com.example.team11project.data.repository.CategoryRepositoryImpl;
import com.example.team11project.data.repository.LevelInfoRepositoryImpl;
import com.example.team11project.data.repository.TaskInstanceRepositoryImpl;
import com.example.team11project.data.repository.TaskRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Boss;
import com.example.team11project.domain.model.ChlothingEffectType;
import com.example.team11project.domain.model.Clothing;
import com.example.team11project.domain.model.Equipment;
import com.example.team11project.domain.model.Potion;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.BossBattle;
import com.example.team11project.domain.model.Weapon;
import com.example.team11project.domain.repository.BossBattleRepository;
import com.example.team11project.domain.repository.BossRepository;
import com.example.team11project.domain.repository.BossRewardRepository;
import com.example.team11project.domain.repository.CategoryRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.TaskInstanceRepository;
import com.example.team11project.domain.repository.TaskRepository;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.BossUseCase;
import com.example.team11project.domain.usecase.TaskUseCase;

import java.util.ArrayList;
import java.util.List;

public class EquipmentViewModel extends ViewModel {
    private final BossUseCase bossUseCase;
    private final UserRepository userRepository;

    private final BossRepository bossRepository;

    // promenljiva verzija
    private final MutableLiveData<BossBattle> _bossBattle = new MutableLiveData<>();
    // nepromenljiva verzija za ui
    public final LiveData<BossBattle> bossBattle = _bossBattle;

    // za stanje ucitavanja
    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>();
    public final LiveData<Boolean> isLoading = _isLoading;

    // za greske
    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public final LiveData<User> user = _user;

    private final MutableLiveData<List<Potion>> _potion = new MutableLiveData<>();
    public final LiveData<List<Potion>> potion = _potion;

    private final MutableLiveData<List<Clothing>> _clothing = new MutableLiveData<>();
    public final LiveData<List<Clothing>> clothing = _clothing;

    private final MutableLiveData<List<Weapon>> _weapon = new MutableLiveData<>();
    public final LiveData<List<Weapon>> weapon = _weapon;



    public EquipmentViewModel(UserRepository userRepository, BossRepository bossRepository, BossUseCase battleService) {
        this.userRepository = userRepository;
        this.bossUseCase = battleService;
        this.bossRepository = bossRepository;
    }

    public void startBossFight(String userId) {
        _isLoading.setValue(true);
        _error.setValue(null); // resetuj greške

        // Prvo dobijamo user podatke
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                // Zatim dobijamo boss podatke
                bossUseCase.findUndefeatedBossRecursive(userId, user.getLevelInfo().getLevel(), new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss boss) {
                        // Konačno kreiramo ili dobijamo postojeći BossBattle
                        bossUseCase.getOrCreateBossBattle(user, boss, new RepositoryCallback<BossBattle>() {
                            @Override
                            public void onSuccess(BossBattle bossBattle) {
                                _bossBattle.postValue(bossBattle);
                                _isLoading.postValue(false);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                _error.postValue("Greška pri kreiranju bitke: " + e.getMessage());
                                _isLoading.postValue(false);
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        _error.postValue("Greška pri učitavanju boss-a: " + e.getMessage());
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

    // resetuje greške kada korisnik vidi poruku
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
            if (modelClass.isAssignableFrom(EquipmentViewModel.class)) {
                try {
                    UserRepository userRepo = new UserRepositoryImpl(application);
                    BossRepository bossRepository = new BossRepositoryImpl(application);
                    BossBattleRepository battleRepository = new BossBattleRepositoryImpl(application);
                    BossRewardRepository rewardRepository = new BossRewardRepositoryImpl(application);
                    BossUseCase bossUseCase = new BossUseCase(bossRepository, battleRepository, rewardRepository);


                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    UserRepository.class,
                                    BossRepository.class,
                                    BossUseCase.class)
                            .newInstance(userRepo, bossRepository, bossUseCase);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public void loadUserEquipment(String userId) {
        _isLoading.setValue(true);
        userRepository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                _user.postValue(user);

                List<Weapon> allWeapon = new ArrayList<>();
                List<Potion> allPotion = new ArrayList<>();
                List<Clothing> allClothing = new ArrayList<>();

                if (user.getWeapons() != null) allWeapon.addAll(user.getWeapons());
                if (user.getPotions() != null) allPotion.addAll(user.getPotions());
                if (user.getClothing() != null) allClothing.addAll(user.getClothing());

                _weapon.postValue(allWeapon);
                _potion.postValue(allPotion);
                _clothing.postValue(allClothing);


                _isLoading.postValue(false);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Greska pri ucitavanju opreme: " + e.getMessage());
                _clothing.postValue(null);
                _weapon.postValue(null);
                _potion.postValue(null);

                _isLoading.postValue(false);
            }
        });
    }
    public void activateEquipment(List<Weapon> selectedWeapons, List<Potion> selectedPotions, List<Clothing> selectedClothing) {
        User currentUser = _user.getValue();
        if (currentUser == null) {
            _error.postValue("Korisnik nije učitan");
            return;
        }

        if (currentUser.getWeapons() != null && selectedWeapons != null) {
            for (Weapon w : selectedWeapons) {
                if (w.getQuantity() <= 0) continue; // nemoj aktivirati ako nema na stanju
                w.setActive(true);
                currentUser.getLevelInfo().setPp(w.getPermanentBoostPercent() + currentUser.getLevelInfo().getPp());
                w.setQuantity(w.getQuantity() - 1);
            }
        }

        if (currentUser.getPotions() != null && selectedPotions != null) {
            for (Potion p : selectedPotions) {
                if (p.getQuantity() <= 0) continue;
                p.setActive(true);
                if (p.isPermanent()) {
                    currentUser.getLevelInfo().setPp(p.getPowerBoostPercent() + currentUser.getLevelInfo().getPp());
                } else {
                    //TODO: dodaj logiku za privremeno povecanje
                    p.setQuantity(p.getQuantity() - 1);
                }
            }
        }

        if (currentUser.getClothing() != null && selectedClothing != null) {
            for (Clothing c : selectedClothing) {
                if (c.getQuantity() <= 0) continue;
                c.setActive(true);
                if(c.getEffectType() == ChlothingEffectType.STRENGTH)
                    currentUser.getLevelInfo().setPp(c.getEffectPercent() + currentUser.getLevelInfo().getPp());
                else if(c.getEffectType() == ChlothingEffectType.SUCCESS_RATE) continue;
                    //TODO: dodaj logiku
                else continue;
                    //TODO: dodaj logiku
                c.setRemainingBattles(2);
                c.setQuantity(c.getQuantity() - 1);
            }
        }

        userRepository.updateUser(currentUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _user.postValue(currentUser);

                _weapon.postValue(currentUser.getWeapons());
                _potion.postValue(currentUser.getPotions());
                _clothing.postValue(currentUser.getClothing());
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue("Neuspesna aktivacija: " + e.getMessage());
            }
        });



    }

}
