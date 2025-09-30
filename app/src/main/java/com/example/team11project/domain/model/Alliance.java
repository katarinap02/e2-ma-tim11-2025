package com.example.team11project.domain.model;

import java.util.List;

public class Alliance {
    private String id;
    private String name;
    private List<String> members;
    private String leader;
    boolean missionActive;

    public Alliance() {}

    public Alliance(String id, String name, List<String> members, String leader, boolean missionActive) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.leader = leader;
        this.missionActive = missionActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public boolean isMissionActive() {
        return missionActive;
    }

    public void setMissionActive(boolean missionActive) {
        this.missionActive = missionActive;
    }
}
