package com.example.team11project.domain.model;

public class AllianceInvite {
    private String id;
    private Alliance alliance;
    private User fromUser;
    private User toUser;
    private boolean accepted;
    private boolean responded;

    public AllianceInvite() {}

    public AllianceInvite(String id, Alliance alliance, User fromUser, User toUser, boolean accepted, boolean responded) {
        this.id = id;
        this.alliance = alliance;
        this.fromUser = fromUser;
        this.toUser = toUser;
        this.accepted = accepted;
        this.responded = responded;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    public void setAlliance(Alliance alliance) {
        this.alliance = alliance;
    }

    public User getFromUser() {
        return fromUser;
    }

    public void setFromUser(User fromUser) {
        this.fromUser = fromUser;
    }

    public User getToUser() {
        return toUser;
    }

    public void setToUser(User toUser) {
        this.toUser = toUser;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isResponded() {
        return responded;
    }

    public void setResponded(boolean responded) {
        this.responded = responded;
    }
}
