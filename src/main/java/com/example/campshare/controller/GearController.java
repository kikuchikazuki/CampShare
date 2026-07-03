package com.example.campshare.controller;

import com.example.campshare.entity.Gear;
import com.example.campshare.service.GearService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/gears")
public class GearController {
  private final GearService gearService;

  public GearController(GearService gearService) {
    this.gearService = gearService;
  }

  @GetMapping
  public String gears(Model model) {
    List<Gear> gears = gearService.selectAll();
    model.addAttribute("gears", gears);
    return "gears/gear-list";
  }
}
