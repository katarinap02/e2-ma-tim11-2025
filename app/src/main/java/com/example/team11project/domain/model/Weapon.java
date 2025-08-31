package com.example.team11project.domain.model;

public class Weapon extends Equipment{
    private int permanentBoostPercent;
    private double upgradeProbability;

    public Weapon(String id, String name, String userId, int permanentBoostPercent) {
        this.id = id;
        this.name = name;
        this.type = EquipmentType.WEAPON;
        this.isActive = true;
        this.permanentBoostPercent = permanentBoostPercent;
        this.upgradeProbability = 0.0;
        this.userId = userId;
    }

    @Override
    public void activate() {
        isActive = true;
    }

    @Override
    public void deactivate() {
        isActive = false;
    }

    public int getPermanentBoostPercent() {
        return permanentBoostPercent;
    }

    public void setPermanentBoostPercent(int permanentBoostPercent) {
        this.permanentBoostPercent = permanentBoostPercent;
    }

    public double getUpgradeProbability() {
        return upgradeProbability;
    }

    public void setUpgradeProbability(double upgradeProbability) {
        this.upgradeProbability = upgradeProbability;
    }
}
