package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.AllianceMission;

public interface AllianceMissionRepository {
    void createAllianceMission(AllianceMission mission, RepositoryCallback<String> callback);
    void getActiveMissionByAllianceId(String allianceId, RepositoryCallback<AllianceMission> callback);
    void getAllianceMissionById(String missionId, RepositoryCallback<AllianceMission> callback);
    void updateBossHp(String missionId, int newHp, RepositoryCallback<Void> callback);
    void updateAllianceMission(AllianceMission mission, RepositoryCallback<Void> callback);
}
