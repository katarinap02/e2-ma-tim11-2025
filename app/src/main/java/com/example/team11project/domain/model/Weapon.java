package com.example.team11project.domain.model;

public class Weapon extends Equipment{
    private int permanentBoostPercent;
    private double upgradeChance;
    private WeaponEffectType effectType;

    public Weapon() {}

    public Weapon(String id, String name, double price, int permanentBoostPercent, double upgradeChance, boolean isActive, WeaponEffectType effectType) {
        super(id, name, EquipmentType.WEAPON, price, isActive);
        this.permanentBoostPercent = permanentBoostPercent;
        this.upgradeChance = upgradeChance;
        this.effectType = effectType;
    }

    @Override
    public void activate(){}

    public void upgrade() {
        this.upgradeChance += 0.01;
    }

    public int getPermanentBoostPercent() {
        return permanentBoostPercent;
    }

    public void setPermanentBoostPercent(int permanentBoostPercent) {
        this.permanentBoostPercent = permanentBoostPercent;
    }

    public double getUpgradeChance() {
        return upgradeChance;
    }

    public void setUpgradeChance(double upgradeChance) {
        this.upgradeChance = upgradeChance;
    }

    public WeaponEffectType getEffectType() {
        return effectType;
    }

    public void setEffectType(WeaponEffectType effectType) {
        this.effectType = effectType;
    }
}
