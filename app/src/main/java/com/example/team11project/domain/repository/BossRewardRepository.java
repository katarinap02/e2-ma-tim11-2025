package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.BossReward;

import java.util.List;

public interface BossRewardRepository {
    void addReward(BossReward category, RepositoryCallback<Void> callback);
    void getRewards(String userId, RepositoryCallback<List<BossReward>> callback);

    void updateReward(BossReward reward, RepositoryCallback<Void> callback);
}
