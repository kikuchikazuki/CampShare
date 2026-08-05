package com.example.campshare.web;

import com.example.campshare.gear.GearRepository;
import com.example.campshare.gear.Gear;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class GearController {
    private static final int PAGE_SIZE = 15;
    private final GearRepository gearRepository;
    public GearController(GearRepository gearRepository) { this.gearRepository = gearRepository; }
    @GetMapping("/gears")
    public String gearList(@RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           Model model) {
        String query = q == null ? "" : q.trim();
        int requestedPage = Math.max(page, 0);
        Page<Gear> result = findPage(query, PageRequest.of(requestedPage, PAGE_SIZE));
        if (result.getTotalPages() > 0 && requestedPage >= result.getTotalPages()) {
            result = findPage(query, PageRequest.of(result.getTotalPages() - 1, PAGE_SIZE));
        }
        model.addAttribute("query", query);
        model.addAttribute("gears", result.getContent());
        model.addAttribute("pageNumber", result.getNumber());
        model.addAttribute("totalPages", result.getTotalPages());
        return "gears";
    }

    private Page<Gear> findPage(String query, Pageable pageable) {
        return query.isEmpty()
                ? gearRepository.findAllByOrderByIdAsc(pageable)
                : gearRepository.search(query, pageable);
    }

    @GetMapping("/gears/{gearId}")
    public String gearDetail(@PathVariable Long gearId, Model model) {
        model.addAttribute("gear", gearRepository.findById(gearId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "用品が見つかりません。")));
        return "gear-detail";
    }
}
