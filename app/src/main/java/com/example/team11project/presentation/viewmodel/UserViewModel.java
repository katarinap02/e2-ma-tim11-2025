package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.AllianceMissionRepository;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.FriendsUseCase;

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository repository;
    private FriendsUseCase friendsUseCase;
    private AllianceMissionRepository allianceMissionRepository;
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();
    private MutableLiveData<Integer> badgeCount = new MutableLiveData<>();

    // LiveData za listu prijatelja
    private MutableLiveData<List<User>> friends = new MutableLiveData<>();

    public LiveData<List<User>> getFriendsLiveData() { return friends; }

    // LiveData za listu korisnika koji nisu prijatelji
    private MutableLiveData<List<User>> nonFriends = new MutableLiveData<>();

    public LiveData<List<User>> getNonFriendsLiveData() { return nonFriends; }


    public LiveData<String> getError() { return error; }

    public UserViewModel(UserRepository repository, AllianceMissionRepository allianceMissionRepository){
        this.repository = repository;
        this.friendsUseCase = friendsUseCase = new FriendsUseCase(repository);
        this.allianceMissionRepository = allianceMissionRepository;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
    }

    public LiveData<Boolean> getUpdateSuccess() {
        return updateSuccess;
    }

    public LiveData<Integer> getBadgeCount() {return  badgeCount; }


    public void loadUser(String userId) {
        repository.getUserById(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();

                error.postValue(e.getMessage());
            }
        });
    }

    public void getUserById(String id, RepositoryCallback<User> callback) {
        repository.getUserById(id, callback);
    }


    public void loadAllUsers() {
        repository.getAllUsers(new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                allUsers.postValue(users);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void getBadgeCount(String userId) {
        allianceMissionRepository.getTotalBadgeCount(userId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer count) {
                badgeCount.postValue(count);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }


    public void updatePassword(String userId, String newPassword) {
        repository.updatePassword(userId, newPassword, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.d("DEBUG", "ViewModel: password update success");

                updateSuccess.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("DEBUG", "ViewModel: password update failed", e);

                error.postValue(e.getMessage());
            }
        });
    }


    public static class Factory implements ViewModelProvider.Factory {
        private final UserRepository userRepository;
        private final AllianceMissionRepository allianceMissionRepository;

        public Factory(UserRepository userRepository, AllianceMissionRepository allianceMissionRepository) {
            this.userRepository = userRepository;
            this.allianceMissionRepository = allianceMissionRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(UserViewModel.class)) {
                return (T) new UserViewModel(userRepository, allianceMissionRepository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }

    public void loadFriends(String currentUserId) {
        friendsUseCase.getFriends(currentUserId, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                friends.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void loadNonFriends(String currentUserId) {
        friendsUseCase.getNonFriends(currentUserId, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                nonFriends.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void addFriend(String currentUserId, String newFriendId) {
        friendsUseCase.addFriend(currentUserId, newFriendId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                loadFriends(currentUserId);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue(e.getMessage());
            }
        });
    }


    public void updateUser(User updatedUser) {
        repository.updateUser(updatedUser, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                updateSuccess.postValue(true);
                user.postValue(updatedUser);
            }

            @Override
            public void onFailure(Exception e) {
                error.postValue("Neuspešna promena korisnika: " + e.getMessage());
            }
        });
    }


}
