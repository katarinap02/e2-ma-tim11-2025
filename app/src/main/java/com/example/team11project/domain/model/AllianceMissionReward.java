package com.example.team11project.domain.model;

public class AllianceMissionReward {
    private String id;
    private String userId;
    private Potion potion;
    private Clothing clothing;
    private int coins;
    private int badgeCount;

    public AllianceMissionReward(){}

    public AllianceMissionReward(String id, String userId, Potion potion, int coins, Clothing clothing, int badgeCount) {
        this.id = id;
        this.userId = userId;
        this.potion = potion;
        this.coins = coins;
        this.clothing = clothing;
        this.badgeCount = badgeCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Potion getPotion() {
        return potion;
    }

    public void setPotion(Potion potion) {
        this.potion = potion;
    }

    public Clothing getClothing() {
        return clothing;
    }

    public void setClothing(Clothing clothing) {
        this.clothing = clothing;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getBadgeCount() {
        return badgeCount;
    }

    public void setBadgeCount(int badgeCount) {
        this.badgeCount = badgeCount;
    }
}
