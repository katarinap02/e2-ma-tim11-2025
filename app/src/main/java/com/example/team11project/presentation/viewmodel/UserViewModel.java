package com.example.team11project.presentation.viewmodel;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.LevelInfoRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.FriendsUseCase;

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository repository;
    private FriendsUseCase friendsUseCase;
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    private MutableLiveData<Boolean> updateSuccess = new MutableLiveData<>();

    // LiveData za listu prijatelja
    private MutableLiveData<List<User>> friends = new MutableLiveData<>();

    public LiveData<List<User>> getFriendsLiveData() { return friends; }

    // LiveData za listu korisnika koji nisu prijatelji
    private MutableLiveData<List<User>> nonFriends = new MutableLiveData<>();

    public LiveData<List<User>> getNonFriendsLiveData() { return nonFriends; }


    public LiveData<String> getError() { return error; }

    public UserViewModel(UserRepository repository){
        this.repository = repository;
        this.friendsUseCase = friendsUseCase = new FriendsUseCase(repository);
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

        public Factory(UserRepository userRepository) {
            this.userRepository = userRepository;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (modelClass.isAssignableFrom(UserViewModel.class)) {
                return (T) new UserViewModel(userRepository);
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


}
