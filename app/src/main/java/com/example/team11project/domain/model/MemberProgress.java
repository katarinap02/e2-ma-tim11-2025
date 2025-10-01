package com.example.team11project.domain.model;

import java.util.Date;
import java.util.Set;

public class MemberProgress {
    private String id;
    private String userId;
    private String missionId;
    private int storePurchases; // max 5
    private int regularBossHits; // max 10
    private int easyNormalTasks; // max 10
    private int otherTasks; // max 6
    private boolean noUnresolvedTasks; // 10 HP bonus
    private Set<Date> messageDays;
    private int totalDamageDealt;

    public MemberProgress() {}

    public MemberProgress(String id, String userId, String missionId, int storePurchases, int regularBossHits, int easyNormalTasks, int otherTasks, boolean noUnresolvedTasks, Set<Date> messageDays, int totalDamageDealt) {
        this.id = id;
        this.userId = userId;
        this.missionId = missionId;
        this.storePurchases = storePurchases;
        this.regularBossHits = regularBossHits;
        this.easyNormalTasks = easyNormalTasks;
        this.otherTasks = otherTasks;
        this.noUnresolvedTasks = noUnresolvedTasks;
        this.messageDays = messageDays;
        this.totalDamageDealt = totalDamageDealt;
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

    public String getMissionId() {
        return missionId;
    }

    public void setMissionId(String missionId) {
        this.missionId = missionId;
    }

    public int getStorePurchases() {
        return storePurchases;
    }

    public void setStorePurchases(int storePurchases) {
        this.storePurchases = storePurchases;
    }

    public int getRegularBossHits() {
        return regularBossHits;
    }

    public void setRegularBossHits(int regularBossHits) {
        this.regularBossHits = regularBossHits;
    }

    public int getEasyNormalTasks() {
        return easyNormalTasks;
    }

    public void setEasyNormalTasks(int easyNormalTasks) {
        this.easyNormalTasks = easyNormalTasks;
    }

    public int getOtherTasks() {
        return otherTasks;
    }

    public void setOtherTasks(int otherTasks) {
        this.otherTasks = otherTasks;
    }

    public boolean isNoUnresolvedTasks() {
        return noUnresolvedTasks;
    }

    public void setNoUnresolvedTasks(boolean noUnresolvedTasks) {
        this.noUnresolvedTasks = noUnresolvedTasks;
    }

    public Set<Date> getMessageDays() {
        return messageDays;
    }

    public void setMessageDays(Set<Date> messageDays) {
        this.messageDays = messageDays;
    }

    public int getTotalDamageDealt() {
        return totalDamageDealt;
    }

    public void setTotalDamageDealt(int totalDamageDealt) {
        this.totalDamageDealt = totalDamageDealt;
    }
}
