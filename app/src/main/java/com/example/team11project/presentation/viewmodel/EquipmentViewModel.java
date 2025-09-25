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
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.model.BossBattle;
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
        public Factory(Application application) { this.application = application; }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(EquipmentViewModel.class)) {
                try {
                    UserRepository userRepo = new UserRepositoryImpl(application);
                    BossRepository bossRepository = new BossRepositoryImpl(application);
                    BossBattleRepository battleRepository = new BossBattleRepositoryImpl(application);
                    BossRewardRepository rewardRepository = new BossRewardRepositoryImpl(application);
                    BossUseCase bossUseCase = new BossUseCase(bossRepository,battleRepository,rewardRepository);


                    @SuppressWarnings("unchecked")
                    T viewModel = (T) modelClass.getConstructor(
                                    UserRepository.class,
                                    BossRepository.class,
                                    BossUseCase.class)
                            .newInstance(userRepo, bossRepository,bossUseCase);
                    return viewModel;
                } catch (Exception e) {
                    throw new RuntimeException("Cannot create an instance of " + modelClass, e);
                }
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

}
