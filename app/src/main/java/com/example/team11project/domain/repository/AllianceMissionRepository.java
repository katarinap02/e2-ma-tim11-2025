package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.AllianceMission;
import com.example.team11project.domain.model.AllianceMissionReward;
import com.example.team11project.domain.model.MemberProgress;

import java.util.List;

public interface AllianceMissionRepository {
    void createAllianceMission(AllianceMission mission, RepositoryCallback<String> callback);
    void getActiveMissionByAllianceId(String allianceId, RepositoryCallback<AllianceMission> callback);
    void getAllianceMissionById(String missionId, RepositoryCallback<AllianceMission> callback);
    void updateBossHp(String missionId, int newHp, RepositoryCallback<Void> callback);
    void updateAllianceMission(AllianceMission mission, RepositoryCallback<Void> callback);

    void updateMemberProgress(MemberProgress progress, RepositoryCallback<Void> callback);
    void getMemberProgressByUserId(String missionId, String userId, RepositoryCallback<MemberProgress> callback);

    void createAllianceMissionReward(AllianceMissionReward reward, RepositoryCallback<String> callback);
    void getAllRewardsByUserId(String userId, RepositoryCallback<List<AllianceMissionReward>> callback);
    void getTotalBadgeCount(String userId, RepositoryCallback<Integer> callback);
}
