package com.example.campshare.repository;

import com.example.campshare.entity.Gear;
import com.example.campshare.mapper.GearMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class GearRepository {
  private final GearMapper gearMapper;

  public GearRepository(GearMapper gearMapper) {this.gearMapper = gearMapper;}

  // 閲覧機能
  public List<Gear> selectAll() { return gearMapper.selectAll();}
}
