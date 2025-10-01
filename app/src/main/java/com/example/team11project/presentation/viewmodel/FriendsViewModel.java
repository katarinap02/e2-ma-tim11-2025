package com.example.team11project.presentation.viewmodel;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.EquipmentRepositoryImpl;
import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.EquipmentRepository;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;
import com.example.team11project.domain.usecase.FriendsUseCase;

import java.util.List;

public class FriendsViewModel extends ViewModel {

    private FriendsUseCase friendsUseCase;
    private UserRepository userRepository;
    private MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private MutableLiveData<List<User>> nonFriendsLiveData = new MutableLiveData<>();

    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();


    public FriendsViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.friendsUseCase = new FriendsUseCase(userRepository);
    }

    public LiveData<List<User>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public LiveData<List<User>> getNonFriendsLiveData() {
        return nonFriendsLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }

    public void loadFriends(String currentUserId) {
        friendsUseCase.getFriends(currentUserId, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                friendsLiveData.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

    public void loadNonFriends(String currentUserId) {
        friendsUseCase.getNonFriends(currentUserId, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> data) {
                nonFriendsLiveData.postValue(data);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }



    public void addFriend(String currentUserId, String newFriendId) {
        friendsUseCase.addFriend(currentUserId, newFriendId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                loadFriends(currentUserId);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue(e.getMessage());
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
            if (modelClass.isAssignableFrom(FriendsViewModel.class)) {
                UserRepository repository = new UserRepositoryImpl(application);
                return (T) new FriendsViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
        }
    }

}
