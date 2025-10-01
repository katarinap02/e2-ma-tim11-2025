package com.example.team11project.domain.model;

public class AllianceBoss {
    private String id;
    private int maxHp;
    private int currentHp;
    private int numberOfMembers;

    public AllianceBoss() {}

    public AllianceBoss(String id, int maxHp, int currentHp, int numberOfMembers) {
        this.id = id;
        this.maxHp = maxHp;
        this.currentHp = currentHp;
        this.numberOfMembers = numberOfMembers;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public int getCurrentHp() {
        return currentHp;
    }

    public void setCurrentHp(int currentHp) {
        this.currentHp = currentHp;
    }

    public int getNumberOfMembers() {
        return numberOfMembers;
    }

    public void setNumberOfMembers(int numberOfMembers) {
        this.numberOfMembers = numberOfMembers;
    }
}
