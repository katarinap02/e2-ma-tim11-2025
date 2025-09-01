package com.example.team11project.domain.model;

import java.util.Date;

public class Task {
    private String id;

    private String groupId;
    private String title;
    private String description;
    private String categoryId;

    private String userId;
    private boolean isRecurring; //da li se ponavlja
    private int recurrenceInterval; // 1, 2, 3...
    private RecurrenceUnit recurrenceUnit; // DAY, WEEK
    private Date recurrenceStartDate;
    private Date recurrenceEndDate;
    private TaskDifficulty difficulty;
    private TaskImportance importance;
    private Date executionTime; //vreme izvrsenja zadatka

    private Date completionDate; //kada je zadatak oznacen kao zavrsen
    private TaskStatus status;

    public Task() {}

    public Task(TaskStatus status, Date executionTime, Date completionDate, TaskImportance importance, TaskDifficulty difficulty, Date recurrenceEndDate, Date recurrenceStartDate, RecurrenceUnit recurrenceUnit, int recurrenceInterval, String categoryId, String userId, boolean isRecurring, String description, String title, String id) {
        this.status = status;
        this.executionTime = executionTime;
        this.completionDate = completionDate;
        this.importance = importance;
        this.difficulty = difficulty;
        this.recurrenceEndDate = recurrenceEndDate;
        this.recurrenceStartDate = recurrenceStartDate;
        this.recurrenceUnit = recurrenceUnit;
        this.recurrenceInterval = recurrenceInterval;
        this.categoryId = categoryId;
        this.userId = userId;
        this.isRecurring = isRecurring;
        this.description = description;
        this.title = title;
        this.id = id;
    }

    public Task(Task other) {
        this.id = other.id;
        this.userId = other.userId;
        this.title = other.title;
        this.description = other.description;
        this.categoryId = other.categoryId;
        this.difficulty = other.difficulty;
        this.importance = other.importance;
        this.status = other.status;
        this.executionTime = other.executionTime != null ? new Date(other.executionTime.getTime()) : null;
        this.completionDate = other.completionDate != null ? new Date(other.completionDate.getTime()) : null;
        this.isRecurring = other.isRecurring;
        this.recurrenceInterval = other.recurrenceInterval;
        this.recurrenceUnit = other.recurrenceUnit;
        this.recurrenceStartDate = other.recurrenceStartDate != null ? new Date(other.recurrenceStartDate.getTime()) : null;
        this.recurrenceEndDate = other.recurrenceEndDate != null ? new Date(other.recurrenceEndDate.getTime()) : null;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public int getRecurrenceInterval() {
        return recurrenceInterval;
    }

    public void setRecurrenceInterval(int recurrenceInterval) {
        this.recurrenceInterval = recurrenceInterval;
    }

    public RecurrenceUnit getRecurrenceUnit() {
        return recurrenceUnit;
    }

    public void setRecurrenceUnit(RecurrenceUnit recurrenceUnit) {
        this.recurrenceUnit = recurrenceUnit;
    }

    public Date getRecurrenceStartDate() {
        return recurrenceStartDate;
    }

    public void setRecurrenceStartDate(Date recurrenceStartDate) {
        this.recurrenceStartDate = recurrenceStartDate;
    }

    public Date getRecurrenceEndDate() {
        return recurrenceEndDate;
    }

    public void setRecurrenceEndDate(Date recurrenceEndDate) {
        this.recurrenceEndDate = recurrenceEndDate;
    }

    public TaskDifficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(TaskDifficulty difficulty) {
        this.difficulty = difficulty;
    }

    public TaskImportance getImportance() {
        return importance;
    }

    public void setImportance(TaskImportance importance) {
        this.importance = importance;
    }

    public Date getExecutionTime() {
        return executionTime;
    }

    public void setExecutionTime(Date executionTime) {
        this.executionTime = executionTime;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(Date completionDate) {
        this.completionDate = completionDate;
    }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    // Helper metoda za proveru da li je task deo grupe
    public boolean isPartOfGroup() {
        return groupId != null && !groupId.equals(getId());
    }

    // Helper metoda za postavljanje group ID pri kreiranju
    public void initializeGroupId() {
        if (groupId == null) {
            groupId = getId(); // Koristi vlastiti ID kao group ID
        }
    }

}
