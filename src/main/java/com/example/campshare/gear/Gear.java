package com.example.campshare.gear;

import jakarta.persistence.*;

@Entity
@Table(name = "gears")
public class Gear {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private String description;
    @Column(name = "daily_price") private Integer dailyPrice;
    @Column(name = "stock_count") private Integer stockCount;
    @Column(name = "image_url") private String imageUrl;
    protected Gear() { }
    public Gear(String name, String category, String description, Integer dailyPrice, Integer stockCount, String imageUrl) {
        update(name, category, description, dailyPrice, stockCount, imageUrl);
    }
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public Integer getDailyPrice() { return dailyPrice; }
    public Integer getStockCount() { return stockCount; }
    public String getImageUrl() { return imageUrl; }

    public void decreaseStock() {
        stockCount--;
    }
    public void update(String name, String category, String description, Integer dailyPrice, Integer stockCount, String imageUrl) {
        this.name = name; this.category = category; this.description = description;
        this.dailyPrice = dailyPrice; this.stockCount = stockCount; this.imageUrl = imageUrl;
    }
}
