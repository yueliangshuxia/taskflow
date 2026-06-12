package com.taskflow.entity.enums;

public enum TaskStatus {
    TODO("待办"),
    IN_PROGRESS("进行中"),
    IN_REVIEW("审核中"),
    DONE("已完成");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
