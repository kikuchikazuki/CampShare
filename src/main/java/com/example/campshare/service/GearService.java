package com.example.campshare.service;

import com.example.campshare.entity.Gear;
import com.example.campshare.mapper.GearMapper;
import com.example.campshare.repository.GearRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GearService {
  private final GearRepository gearRepository;

  public GearService(GearRepository gearRepository){ this.gearRepository = gearRepository;};

  // 閲覧機能
  public List<Gear> selectAll() {
    return gearRepository.selectAll();
  }
}