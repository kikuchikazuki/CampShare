package com.example.campshare.entity;

import lombok.Data;

import java.security.Timestamp;

@Data
public class Gear {

  private long id;
  private String name;
  private String category;
  private String description;
  private int stock;
  private String imageUrl;
  private Timestamp createdAt;

}
