package com.example.campshare.web;

import com.example.campshare.gear.GearRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class GearController {
    private final GearRepository gearRepository;
    public GearController(GearRepository gearRepository) { this.gearRepository = gearRepository; }
    @GetMapping("/gears")
    public String gearList(Model model) {
        model.addAttribute("gears", gearRepository.findAllByOrderByIdAsc());
        return "gears";
    }
}
