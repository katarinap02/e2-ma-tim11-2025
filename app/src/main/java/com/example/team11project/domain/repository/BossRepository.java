package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Boss;

import java.util.List;

public interface BossRepository {
    void addBoss(Boss category, RepositoryCallback<Void> callback);
    void getBosses(String userId, RepositoryCallback<List<Boss>> callback);

    void getBossById(String userId, String bossId, RepositoryCallback<Boss> callback);
    void updateBoss(Boss boss, RepositoryCallback<Void> callback);
    public void getBossByUserIdAndLevel(String userId, int level, RepositoryCallback<Boss> callback);
}
