package com.example.bookhub;

public class User {
    private String userId;
    private String name;
    private String email;
    private String phone;
    private boolean isAdmin;
    private boolean isVerified;

    public User() {}

    public User(String userId, String name, String email, String phone) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = false;
        this.isVerified = false;
    }

    public User(String userId, String name, String email, String phone, boolean isAdmin, boolean isVerified) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.isAdmin = isAdmin;
        this.isVerified = isVerified;
    }

    public String getUserId() { return userId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isAdmin() { return isAdmin; }
    public void setAdmin(boolean admin) { isAdmin = admin; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }
}