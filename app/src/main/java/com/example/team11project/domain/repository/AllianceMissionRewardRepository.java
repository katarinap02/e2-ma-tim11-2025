package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.AllianceMissionReward;

import java.util.List;

public interface AllianceMissionRewardRepository {

    void createAllianceMissionReward(AllianceMissionReward reward, RepositoryCallback<String> callback);
    void getAllRewardsByUserId(String userId, RepositoryCallback<List<AllianceMissionReward>> callback);
    void getTotalBadgeCount(String userId, RepositoryCallback<Integer> callback);
}
