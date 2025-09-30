package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.Alliance;
import com.example.team11project.domain.model.AllianceInvite;

import java.util.List;

public interface AllianceRepository {
    void addAlliance(Alliance alliance, RepositoryCallback<Void> callback);
    void getAllAlliances(String userId, RepositoryCallback<List<Alliance>> callback);
    void getAllianceById(String userId, String allianceId, RepositoryCallback<Alliance> callback);
    void updateAlliance(Alliance alliance, RepositoryCallback<Void> callback);
    void deleteAlliance(String allianceId, String userId, RepositoryCallback<Void> callback);

    void getAllianceInviteById(String userId, String allianceInviteId, RepositoryCallback<AllianceInvite> callback);

    void addAllianceInvite(AllianceInvite invite, RepositoryCallback<Void> callback);
    void getAllAllianceInvites(String userId, RepositoryCallback<List<AllianceInvite>> callback);
    void updateAllianceInvite(AllianceInvite invite, RepositoryCallback<Void> callback);
    void deleteAllianceInvite(String inviteId, String userId, RepositoryCallback<Void> callback);
    void getPendingInvites(String userId, RepositoryCallback<List<AllianceInvite>> callback);
    void acceptInvite(String userId, String inviteId, RepositoryCallback<Void> callback);
    void rejectInvite(String userId, String inviteId, RepositoryCallback<Void> callback);
    void disbandAlliance(String allianceId, String leaderId, RepositoryCallback<Void> callback);

}
