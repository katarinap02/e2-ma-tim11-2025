package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.BossBattle;

import java.util.List;

public interface BossBattleRepository {
    void addBattle(BossBattle category, RepositoryCallback<Void> callback);
    void getBattles(String userId, RepositoryCallback<List<BossBattle>> callback);

    void updateBattle(BossBattle battle, RepositoryCallback<Void> callback);
    public void getBattleByUserAndBossAndLevel(String userId, String bossId, int level, RepositoryCallback<BossBattle> callback);
}
