package com.example.team11project.domain.model;

public class BossReward {
    private String id;
    private String bossId;
    private String userId;
    private int level;
    private int coinsEarned;
    private String equipmentId;

    public BossReward(){}

    public BossReward(String id, String bossId, String userId, int level, int coinsEarned, String equipmentId) {
        this.id = id;
        this.bossId = bossId;
        this.userId = userId;
        this.level = level;
        this.coinsEarned = coinsEarned;
        this.equipmentId = equipmentId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getCoinsEarned() {
        return coinsEarned;
    }

    public void setCoinsEarned(int coinsEarned) {
        this.coinsEarned = coinsEarned;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
