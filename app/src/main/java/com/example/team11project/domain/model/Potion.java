package com.example.team11project.domain.model;

public class Potion extends Equipment{
    private boolean isPermanent;
    private int powerBoostPercent;

    public Potion() {}

    public Potion(String id, String name, double price, int powerBoostPercent, boolean isPermanent, boolean isActive, int quantity) {
        super(id, name, EquipmentType.POTION, price, isActive, quantity);
        this.powerBoostPercent = powerBoostPercent;
        this.isPermanent = isPermanent;
    }

    @Override
    public void activate(){}
    public boolean isPermanent() {
        return isPermanent;
    }

    public void setPermanent(boolean permanent) {
        isPermanent = permanent;
    }

    public int getPowerBoostPercent() {
        return powerBoostPercent;
    }

    public void setPowerBoostPercent(int powerBoostPercent) {
        this.powerBoostPercent = powerBoostPercent;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
