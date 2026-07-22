package com.example.campshare.web;

import com.example.campshare.gear.GearRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class GearController {
    private final GearRepository gearRepository;
    public GearController(GearRepository gearRepository) { this.gearRepository = gearRepository; }
    @GetMapping("/gears")
    public String gearList(Model model) {
        model.addAttribute("gears", gearRepository.findAllByOrderByIdAsc());
        return "gears";
    }

    @GetMapping("/gears/{gearId}")
    public String gearDetail(@PathVariable Long gearId, Model model) {
        model.addAttribute("gear", gearRepository.findById(gearId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品が見つかりません。")));
        return "gear-detail";
    }
}
