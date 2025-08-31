package com.example.team11project.domain.model;

public abstract class Equipment {
    protected String id;
    protected String name;
    protected EquipmentType type;
    protected int cost;
    protected boolean isActive;
    protected String userId;

    /*public Equipment(String name, EquipmentType type, int cost, boolean isActive){
        this.name = name;
        this.type = type;
        this.cost = cost;
        this.isActive = isActive;
    }
*/
    public abstract void activate();
    public abstract void deactivate();

    public String getName() {
        return name;
    }

    public EquipmentType getType() {
        return type;
    }

    public int getCost() {
        return cost;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public void setCost(int cost) {
        this.cost = cost;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
