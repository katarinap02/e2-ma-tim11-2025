package com.example.team11project.domain.model;

public class MissionFinalizationResult {
    private boolean wasFinalized;
    private boolean bossDefeated;

    public MissionFinalizationResult() {}

    public MissionFinalizationResult(boolean wasFinalized, boolean bossDefeated) {
        this.wasFinalized = wasFinalized;
        this.bossDefeated = bossDefeated;
    }

    public boolean isWasFinalized() {
        return wasFinalized;
    }

    public void setWasFinalized(boolean wasFinalized) {
        this.wasFinalized = wasFinalized;
    }

    public boolean isBossDefeated() {
        return bossDefeated;
    }

    public void setBossDefeated(boolean bossDefeated) {
        this.bossDefeated = bossDefeated;
    }
}
