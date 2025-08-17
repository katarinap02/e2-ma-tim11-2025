package com.example.team11project.presentation.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.team11project.data.repository.UserRepositoryImpl;
import com.example.team11project.domain.model.Category;
import com.example.team11project.domain.model.User;
import com.example.team11project.domain.repository.RepositoryCallback;
import com.example.team11project.domain.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

public class RegisterViewModel extends ViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<List<User>> _users = new MutableLiveData<>();
    private final MutableLiveData<String> _error = new MutableLiveData<>();

    public RegisterViewModel(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public LiveData<List<User>> getUsers() {
        return _users;
    }

    public LiveData<String> getError() {
        return _error;
    }

    public void registerUser(String username, String password, String mail, String avatar){
        User user = new User();

        user.setUsername(username);
        user.setPassword(password);
        user.setMail(mail);
        user.setAvatar(avatar);

        userRepository.addUser(user, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                List<User> currentList = _users.getValue();

                if (currentList == null) {
                    currentList = new ArrayList<>();
                }
                currentList.add(user);
                _users.postValue(currentList);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
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
            if (modelClass.isAssignableFrom(RegisterViewModel.class)) {
                UserRepository repository = new UserRepositoryImpl(application);
                return (T) new RegisterViewModel(repository);
            }
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
