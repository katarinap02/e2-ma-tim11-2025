package com.example.team11project.domain.model;

public class Potion extends Equipment{
    private boolean isOneTimeUse;
    private double powerBoost;
    private boolean isConsumed;


    public Potion(String id, String name, String userId, int powerBoost, boolean isOneTimeUse) {
        this.id = id;
        this.name = name;
        this.type = EquipmentType.POTION;
        this.isActive = false;
        this.isConsumed = false;
        this.powerBoost = powerBoost;
        this.isOneTimeUse = isOneTimeUse;
        this.userId = userId;
    }

    @Override
    public void activate(){
        if(isOneTimeUse) {
            if(isOneTimeUse){
                throw new IllegalStateException("Napitak je vec bio iskoriscen, i ne moze ponovo da se aktivira");
            }
            isActive = true;
        } else{
            isActive = true;
        }
    }

    @Override
    public void deactivate(){
        if(isOneTimeUse){
            isActive = false;
            isConsumed = true;
        } else{
            isActive = false;
        }
    }

    public boolean isOneTimeUse() {
        return isOneTimeUse;
    }

    public double getPowerBoost() {
        return powerBoost;
    }

    public boolean isConsumed() {
        return isConsumed;
    }

    public void setOneTimeUse(boolean oneTimeUse) {
        isOneTimeUse = oneTimeUse;
    }

    public void setPowerBoost(double powerBoost) {
        this.powerBoost = powerBoost;
    }

    public void setConsumed(boolean consumed) {
        isConsumed = consumed;
    }
}
