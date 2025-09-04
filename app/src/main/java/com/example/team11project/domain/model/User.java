package com.example.team11project.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class User {
    private String id;
    private String username;
    private String email;
    private String password;
    private String avatar;
    private boolean isVerified;
    private LevelInfo levelInfo;
    private List<Equipment> equipment;
    private int coins;

    public User(){}

    public User(String id, String username, String email, String password, String avatar, boolean isVerified, int coins){
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.avatar = avatar;
        this.isVerified = isVerified;
        levelInfo = new LevelInfo();
        equipment = new ArrayList<>();
        this.coins = coins;
    }

    public String getId(){
        return id;
    }

    public void setId(String id){
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean getVerified() {
        return isVerified;
    }

    public void setVerified(boolean verified) {
        isVerified = verified;
    }

    public LevelInfo getLevelInfo() {
        return levelInfo;
    }

    public void setLevelInfo(LevelInfo levelInfo) {
        this.levelInfo = levelInfo;
    }

    public List<Equipment> getEquipment() {
        return equipment;
    }

    public void setEquipment(List<Equipment> equipment) {
        this.equipment = equipment;
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }
}
