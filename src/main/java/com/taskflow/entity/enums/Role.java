package com.taskflow.entity.enums;

public enum Role {
    ADMIN("管理员"),
    USER("普通用户");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
