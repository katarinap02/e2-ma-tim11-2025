package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.User;

public interface UserRepository {
    void addUser(User user, RepositoryCallback<Void> callback);

    void getUser(String userId, RepositoryCallback<Void> callback);
}
