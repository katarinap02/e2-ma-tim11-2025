package com.example.team11project.domain.model;

public enum TaskImportance {
    NORMAL(1),
    IMPORTANT(3),
    VERY_IMPORTANT(10),
    SPECIAL(100);

    private final int xpValue;

    TaskImportance(int xpValue) {
        this.xpValue = xpValue;
    }

    public int getXpValue() {
        return xpValue;
    }


}
