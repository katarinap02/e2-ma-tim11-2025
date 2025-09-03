package com.example.team11project.domain.model;

public class Boss {
    private String id;
    private String userId;
    private int level;
    private int maxHP;
    private int currentHP;
    private boolean isDefeated;
    private int coinsReward;

    public Boss(){}
    public Boss(String id, String userId,int level, int maxHP, boolean isDefeated, int coinsReward, int currentHP) {
        this.id = id;
        this.userId = id;
        this.level = level;
        this.maxHP = maxHP;
        this.isDefeated = isDefeated;
        this.coinsReward = coinsReward;
        this.currentHP = currentHP;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getMaxHP() {
        return maxHP;
    }

    public void setMaxHP(int maxHP) {
        this.maxHP = maxHP;
    }

    public int getCurrentHP() {
        return currentHP;
    }

    public void setCurrentHP(int currentHP) {
        this.currentHP = currentHP;
    }

    public boolean isDefeated() {
        return isDefeated;
    }

    public void setDefeated(boolean defeated) {
        isDefeated = defeated;
    }

    public int getCoinsReward() {
        return coinsReward;
    }

    public void setCoinsReward(int coinsReward) {
        this.coinsReward = coinsReward;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

