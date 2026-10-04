package com.example.bookhub;

public class Notification {
    private String id;
    private String title;
    private String message;
    private long timestamp;
    private boolean read;
    private String type; // "BOOK_APPROVAL", "USER_VERIFICATION", "SUPPORT_REQUEST", "INFO"
    private String targetId;

    public Notification() {}

    public Notification(String id, String title, String message, long timestamp) {
        this(id, title, message, timestamp, "INFO", "");
    }

    public Notification(String id, String title, String message, long timestamp, String type, String targetId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
        this.read = false;
        this.type = type;
        this.targetId = targetId;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
    public boolean isRead() { return read; }
    public void setRead(boolean read) { this.read = read; }
    public String getType() { return type; }
    public String getTargetId() { return targetId; }
}