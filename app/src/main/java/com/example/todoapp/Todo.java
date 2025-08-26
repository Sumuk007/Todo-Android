package com.example.todoapp;

public class Todo {
    private String id;
    private String title;
    private String status; // "in_progress" or "completed"
    private String userId;

    public Todo() {
        // Needed for Firebase
    }

    public Todo(String id, String title, String status, String userId) {
        this.id = id;
        this.title = title;
        this.status = status;
        this.userId = userId;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getStatus() { return status; }
    public String getUserId() { return userId; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setStatus(String status) { this.status = status; }
    public void setUserId(String userId) { this.userId = userId; }
}
