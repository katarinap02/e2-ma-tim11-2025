package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.MemberProgress;

public interface MemberProgressRepository {
    void updateMemberProgress(MemberProgress progress, RepositoryCallback<Void> callback);
    void getMemberProgressByUserId(String missionId, String userId, RepositoryCallback<MemberProgress> callback);
}
