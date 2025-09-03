package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.BossBattle;

import java.util.List;

public interface BossBattleRepository {
    void addBattle(BossBattle category, RepositoryCallback<Void> callback);
    void getBattles(String userId, RepositoryCallback<List<BossBattle>> callback);

    void updateBattle(BossBattle category, RepositoryCallback<Void> callback);
}
