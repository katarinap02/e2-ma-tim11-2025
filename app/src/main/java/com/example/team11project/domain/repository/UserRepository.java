package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.LoginResult;
import com.example.team11project.domain.model.User;

import java.util.List;

public interface UserRepository {
    void addUser(User user, RepositoryCallback<Void> callback);
    void getUserById(String userId, RepositoryCallback<User> callback);
    LoginResult login(String email, String password, RepositoryCallback<User> callback);
    void getAllUsers(RepositoryCallback<List<User>> callback);
    void updatePassword(String userId, String newPassword, RepositoryCallback<Void> callback);
}
