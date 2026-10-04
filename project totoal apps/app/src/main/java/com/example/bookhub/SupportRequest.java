package com.example.bookhub;

public class SupportRequest {
    private String id;
    private String userId;
    private String userName;
    private String queryType;
    private String phone;
    private String description;
    private long timestamp;
    private boolean resolved;

    public SupportRequest() {}

    public SupportRequest(String id, String userId, String userName, String queryType, String phone, String description, long timestamp) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.queryType = queryType;
        this.phone = phone;
        this.description = description;
        this.timestamp = timestamp;
        this.resolved = false;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getUserName() { return userName; }
    public String getQueryType() { return queryType; }
    public String getPhone() { return phone; }
    public String getDescription() { return description; }
    public long getTimestamp() { return timestamp; }
    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }
}