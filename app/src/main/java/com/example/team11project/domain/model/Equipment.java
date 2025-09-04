package com.example.team11project.domain.model;

public abstract class Equipment {
    protected String id;
    protected String name;
    protected EquipmentType type;
    protected double price;
    protected boolean isActive;

    public Equipment(){}

    public Equipment(String id, String name, EquipmentType type, double price, boolean isActive) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.price = price;
        this.isActive = isActive;
    }
    public abstract void activate();

    public String getName() {
        return name;
    }

    public EquipmentType getType() {
        return type;
    }

    public double getPrice() {
        return price;
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

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
