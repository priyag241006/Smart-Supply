package com.inventory.model;

import java.time.LocalDate;

public class Product {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private int threshold;
    private double price;
    private LocalDate expiryDate;

    public Product() {}

    public Product(String id, String name, String category,
                   int quantity, int threshold, double price, LocalDate expiryDate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.threshold = threshold;
        this.price = price;
        this.expiryDate = expiryDate;
    }

    public String getId()                    { return id; }
    public void   setId(String id)           { this.id = id; }
    public String getName()                  { return name; }
    public void   setName(String name)       { this.name = name; }
    public String getCategory()              { return category; }
    public void   setCategory(String c)      { this.category = c; }
    public int    getQuantity()              { return quantity; }
    public void   setQuantity(int qty)       { this.quantity = qty; }
    public int    getThreshold()             { return threshold; }
    public void   setThreshold(int t)        { this.threshold = t; }
    public double getPrice()                 { return price; }
    public void   setPrice(double price)     { this.price = price; }
    public LocalDate getExpiryDate()         { return expiryDate; }
    public void   setExpiryDate(LocalDate d) { this.expiryDate = d; }
}