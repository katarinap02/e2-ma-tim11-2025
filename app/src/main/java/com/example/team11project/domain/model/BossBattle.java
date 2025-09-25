package com.example.team11project.domain.model;

import java.util.List;

public class BossBattle {
    private String id;
    private String userId;
    private String bossId;
    private int level;
    private int attacksUsed;
    private int damageDealt; //koliko je stete naneto bosu
    private double hitChance; //sansa koja se racuna za pogodak
    private List<String> activeEquipment;
    private int userPP;
    private boolean bossDefeated;

    public BossBattle() {}
    public BossBattle(String id, String userId, int level, String bossId, int damageDealt, int attacksUsed, double hitChance, List<String> activeEquipment, int userPP, boolean bossDefeated) {
        this.id = id;
        this.userId = userId;
        this.level = level;
        this.bossId = bossId;
        this.damageDealt = damageDealt;
        this.attacksUsed = attacksUsed;
        this.hitChance = hitChance;
        this.activeEquipment = activeEquipment;
        this.userPP = userPP;
        this.bossDefeated = bossDefeated;
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

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public int getAttacksUsed() {
        return attacksUsed;
    }

    public void setAttacksUsed(int attacksUsed) {
        this.attacksUsed = attacksUsed;
    }

    public double getHitChance() {
        return hitChance;
    }

    public void setHitChance(double hitChance) {
        this.hitChance = hitChance;
    }

    public int getDamageDealt() {
        return damageDealt;
    }

    public void setDamageDealt(int damageDealt) {
        this.damageDealt = damageDealt;
    }

    public List<String> getActiveEquipment() {
        return activeEquipment;
    }

    public void setActiveEquipment(List<String> activeEquipment) {
        this.activeEquipment = activeEquipment;
    }

    public int getUserPP() {
        return userPP;
    }

    public void setUserPP(int userPP) {
        this.userPP = userPP;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }
}
