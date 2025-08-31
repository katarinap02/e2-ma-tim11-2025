package com.example.team11project.domain.model;

public class Clothing extends Equipment{
    private int effectPercent;
    private int remainingBattles;

    public Clothing(String id, String name, String userId, int effectPercent) {
        this.id = id;
        this.name = name;
        this.type = EquipmentType.CLOTHING;
        this.isActive = false;
        this.remainingBattles = 0;
        this.effectPercent = effectPercent;
        this.userId = userId;
    }

    @Override
    public void activate() {
        if(!isActive && remainingBattles > 0){
            isActive = true;
        }
    }

    @Override
    public void deactivate() {
        isActive = false;
    }

    public void onBossFightEnd() {
        if (isActive) {
            remainingBattles--;
            if (remainingBattles <= 0) {
                deactivate();
            }
        }
    }

    public int getEffectPercent() {
        return effectPercent;
    }

    public void setEffectPercent(int effectPercent) {
        this.effectPercent = effectPercent;
    }

    public int getRemainingBattles() {
        return remainingBattles;
    }

    public void setRemainingBattles(int remainingBattles) {
        this.remainingBattles = remainingBattles;
    }
}
