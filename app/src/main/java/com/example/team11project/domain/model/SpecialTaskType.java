package com.example.team11project.domain.model;

public enum SpecialTaskType {
    STORE_PURCHASE(2, 5),           // 2 HP, max 5 puta
    REGULAR_BOSS_HIT(2, 10),        // 2 HP, max 10 puta
    EASY_NORMAL_TASK(1, 10),        // 1 HP, max 10 puta
    OTHER_TASK(4, 6),               // 4 HP, max 6 puta
    NO_UNRESOLVED_TASKS(10, 1),     // 10 HP, samo jednom
    DAILY_MESSAGE(4, Integer.MAX_VALUE); // 4 HP, neograničeno (po danu)

    private final int hpDamage;
    private final int maxCount;

    SpecialTaskType(int hpDamage, int maxCount) {
        this.hpDamage = hpDamage;
        this.maxCount = maxCount;
    }

    public int getHpDamage() {
        return hpDamage;
    }

    public int getMaxCount() {
        return maxCount;
    }
}