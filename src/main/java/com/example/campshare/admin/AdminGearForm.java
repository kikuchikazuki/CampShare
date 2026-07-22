package com.example.campshare.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class AdminGearForm {
 @NotBlank private String name;
 @NotBlank private String category;
 @NotBlank private String description;
 @NotNull @PositiveOrZero private Integer dailyPrice;
 @NotNull @PositiveOrZero private Integer stockCount;
 @NotBlank private String imageUrl;
 public String getName(){return name;} public void setName(String v){name=v;}
 public String getCategory(){return category;} public void setCategory(String v){category=v;}
 public String getDescription(){return description;} public void setDescription(String v){description=v;}
 public Integer getDailyPrice(){return dailyPrice;} public void setDailyPrice(Integer v){dailyPrice=v;}
 public Integer getStockCount(){return stockCount;} public void setStockCount(Integer v){stockCount=v;}
 public String getImageUrl(){return imageUrl;} public void setImageUrl(String v){imageUrl=v;}
}
