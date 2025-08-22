package com.example.team11project.domain.model;

public class LevelInfo {
    private String id;
    private int level;
    private int xp;
    private int xpForNextLevel;
    private int xpTaskImportance;
    private int xpTaskDifficulty;
    private UserTitle title;
    private int pp;

    public LevelInfo(){}

    public LevelInfo(int level, int xpForNextLevel, int xp, int xpTaskImportance, int xpTaskDifficulty, UserTitle title, int pp) {
        this.xpForNextLevel = xpForNextLevel;
        this.level = level;
        this.xp = xp;
        this.xpTaskImportance = xpTaskImportance;
        this.xpTaskDifficulty = xpTaskDifficulty;
        this.title = title;
        this.pp = pp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getXp() {
        return xp;
    }

    public void setXp(int xpTotal) {
        this.xp = xpTotal;
    }

    public int getXpForNextLevel() {
        return xpForNextLevel;
    }

    public void setXpForNextLevel(int xpForNextLevel) {
        this.xpForNextLevel = xpForNextLevel;
    }

    public int getXpTaskImportance() {
        return xpTaskImportance;
    }

    public void setXpTaskImportance(int xpTaskImportance) {
        this.xpTaskImportance = xpTaskImportance;
    }

    public int getXpTaskDifficulty() {
        return xpTaskDifficulty;
    }

    public void setXpTaskDifficulty(int xpTaskDifficulty) {
        this.xpTaskDifficulty = xpTaskDifficulty;
    }

    public UserTitle getTitle() {
        return title;
    }

    public void setTitle(UserTitle title) {
        this.title = title;
    }

    public int getPp() {
        return pp;
    }

    public void setPp(int pp) {
        this.pp = pp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
