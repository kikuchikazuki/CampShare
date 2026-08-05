package com.example.campshare.web;
import com.example.campshare.admin.*;
import com.example.campshare.gear.GearRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

@Controller
@RequestMapping("/admin/gears")
public class AdminGearController {
 private final GearRepository gears; private final AdminGearService service;
 public AdminGearController(GearRepository gears, AdminGearService service){this.gears=gears;this.service=service;}
 @GetMapping public String list(@RequestParam(required = false) String q, Model m){String query=q == null ? "" : q.trim();m.addAttribute("query",query);m.addAttribute("gears",query.isEmpty() ? gears.findAllByOrderByIdAsc(Pageable.unpaged()).getContent() : gears.search(query, Pageable.unpaged()).getContent());return "admin/gears";}
 @GetMapping("/new") public String createForm(Model m){m.addAttribute("gearForm",new AdminGearForm());return "admin/gear-form";}
 @PostMapping public String create(@Valid @ModelAttribute("gearForm") AdminGearForm f, BindingResult r){if(r.hasErrors())return "admin/gear-form";service.create(f);return "redirect:/admin/gears?created=true";}
 @GetMapping("/{id}/edit") public String editForm(@PathVariable Long id, Model m){var g=service.get(id);var f=new AdminGearForm();f.setName(g.getName());f.setCategory(g.getCategory());f.setDescription(g.getDescription());f.setDailyPrice(g.getDailyPrice());f.setStockCount(g.getStockCount());f.setImageUrl(g.getImageUrl());m.addAttribute("gearForm",f);m.addAttribute("gearId",id);return "admin/gear-form";}
 @PostMapping("/{id}") public String update(@PathVariable Long id,@Valid @ModelAttribute("gearForm") AdminGearForm f,BindingResult r,Model m){if(r.hasErrors()){m.addAttribute("gearId",id);return "admin/gear-form";}service.update(id,f);return "redirect:/admin/gears?updated=true";}
 @PostMapping("/{id}/delete") public String delete(@PathVariable Long id){try{service.delete(id);return "redirect:/admin/gears?deleted=true";}catch(GearCannotBeDeletedException e){return "redirect:/admin/gears?deleteError=true";}}
}
