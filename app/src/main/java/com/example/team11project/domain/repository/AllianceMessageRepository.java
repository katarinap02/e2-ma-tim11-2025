package com.example.team11project.domain.repository;

import com.example.team11project.domain.model.AllianceMessage;

import java.util.List;

public interface AllianceMessageRepository {
    void sendMessage(String leaderId, AllianceMessage message, RepositoryCallback<Void> callback);
    void getAllMessages(String allianceLeaderId, String allianceId, RepositoryCallback<List<AllianceMessage>> callback);



}
