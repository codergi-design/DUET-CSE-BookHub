package com.example.bookhub;

public class Book {
    private String id;
    private String title;
    private String author;
    private String price;
    private String semester;
    private String condition;
    private String phone;
    private String sellerId;
    private String sellerName;
    private boolean sold; 
    private String status; // pending, approved, rejected

    public Book() {}

    public Book(String id, String title, String author, String price, String semester, String condition, String phone, String sellerId, String sellerName) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.price = price;
        this.semester = semester;
        this.condition = condition;
        this.phone = phone;
        this.sellerId = sellerId;
        this.sellerName = sellerName;
        this.sold = false; 
        this.status = "pending"; 
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getPrice() { return price; }
    public String getSemester() { return semester; }
    public String getCondition() { return condition; }
    public String getPhone() { return phone; }
    public String getSellerId() { return sellerId; }
    public String getSellerName() { return sellerName; }
    public boolean isSold() { return sold; }
    public void setSold(boolean sold) { this.sold = sold; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}