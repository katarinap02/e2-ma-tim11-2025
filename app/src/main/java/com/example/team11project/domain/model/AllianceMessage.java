package com.example.team11project.domain.model;

import java.util.Date;

public class AllianceMessage {
    private String id;
    private String senderId;
    private String senderUsername;
    private String allianceId;
    private String message;
    private long timestamp;

    public AllianceMessage() {}

    public AllianceMessage(String id, String senderId, String senderUsername, String allianceId, String message, long timestamp) {
        this.id = id;
        this.senderId = senderId;
        this.senderUsername = senderUsername;
        this.allianceId = allianceId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderUsername() { return senderUsername; }
    public String getAllianceId() { return allianceId; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderUsername(String senderUsername) { this.senderUsername = senderUsername; }
    public void setAllianceId(String allianceId) { this.allianceId = allianceId; }
    public void setMessage(String message) { this.message = message; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
