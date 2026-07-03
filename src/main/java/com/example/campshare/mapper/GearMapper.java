package com.example.campshare.mapper;

import com.example.campshare.entity.Gear;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface GearMapper {
  @Select("SELECT * FROM gears ORDER BY id")
  List<Gear> selectAll();
}
