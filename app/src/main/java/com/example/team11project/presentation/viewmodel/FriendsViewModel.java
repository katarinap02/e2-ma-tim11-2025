package com.example.team11project.presentation.viewmodel;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.usecase.FriendsUseCase;

import java.util.List;

public class FriendsViewModel extends ViewModel {

    private FriendsUseCase friendsUseCase;

    private MutableLiveData<List<User>> friendsLiveData = new MutableLiveData<>();
    private MutableLiveData<String> errorLiveData = new MutableLiveData<>();


    public FriendsViewModel(FriendsUseCase friendsUseCase) {
        this.friendsUseCase = friendsUseCase;
    }

    public LiveData<List<User>> getFriendsLiveData() {
        return friendsLiveData;
    }

    public LiveData<String> getErrorLiveData() {
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

    public void addFriend(String currentUserId, String newFriendId) {
        friendsUseCase.addFriend(currentUserId, newFriendId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean data) {
                // refresuj listu prijatelja
                loadFriends(currentUserId);
            }

            @Override
            public void onFailure(Exception e) {
                errorLiveData.postValue(e.getMessage());
            }
        });
    }

}
