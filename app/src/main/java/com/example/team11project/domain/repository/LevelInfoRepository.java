package com.example.team11project.domain.repository;

import com.example.team11project.data.datasource.remote.RemoteDataSource;
import com.example.team11project.domain.model.User;

public interface LevelInfoRepository {
    void addXp(User user, int xpEarned, RepositoryCallback<Void> callback);


}
