package com.example.team11project.domain.model;

import java.util.Date;

public class TaskInstance {
    private String id;
    private String originalTaskId; // Poveznica na originalni Task (FOREIGN KEY)
    private String userId;
    private Date originalDate; // Tačan datum instance (npr. 2025-08-15 00:00:00)
    private TaskStatus newStatus; // Novi status (COMPLETED, CANCELED, UNCOMPLETED)
    private Date completionDate;

    public TaskInstance() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalTaskId() {
        return originalTaskId;
    }

    public void setOriginalTaskId(String originalTaskId) {
        this.originalTaskId = originalTaskId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Date getOriginalDate() {
        return originalDate;
    }

    public void setOriginalDate(Date originalDate) {
        this.originalDate = originalDate;
    }

    public TaskStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(TaskStatus newStatus) {
        this.newStatus = newStatus;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }
}
