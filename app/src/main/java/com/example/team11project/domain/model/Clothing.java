package com.example.team11project.domain.model;

public class Clothing extends Equipment{
    private int effectPercent;
    private ChlothingEffectType effectType;
    private int remainingBattles;

    public Clothing(){}
    public Clothing(String id, String name, double price, int effectPercent, boolean isActive, int quantity, ChlothingEffectType effectType) {
        super(id, name, EquipmentType.CLOTHING, price, isActive, quantity);
        this.remainingBattles = 2;
        this.effectPercent = effectPercent;
        this.effectType = effectType;
    }

    @Override
    public void activate(){
        this.isActive = true;
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

    public ChlothingEffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(ChlothingEffectType effectType) {
        this.effectType = effectType;
    }
}
