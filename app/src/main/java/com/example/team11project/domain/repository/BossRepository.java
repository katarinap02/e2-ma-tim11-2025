package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Boss;

import java.util.List;

public interface BossRepository {
    void addBoss(Boss category, RepositoryCallback<Void> callback);
    void getBosses(String userId, RepositoryCallback<List<Boss>> callback);

    void updateBoss(Boss category, RepositoryCallback<Void> callback);
}
