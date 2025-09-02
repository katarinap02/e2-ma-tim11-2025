package com.example.team11project.presentation.viewmodel;

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

import java.util.List;

public class UserViewModel extends ViewModel {

    private UserRepository repository;
    private MutableLiveData<User> user = new MutableLiveData<>();
    private MutableLiveData<List<User>> allUsers = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();
    public LiveData<String> getError() { return error; }

    public UserViewModel(UserRepository repository){
        this.repository = repository;
    }

    public LiveData<User> getUser() {
        return user;
    }

    public LiveData<List<User>> getAllUsers() {
        return allUsers;
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
}
