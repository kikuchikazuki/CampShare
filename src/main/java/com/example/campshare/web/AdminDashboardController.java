package com.example.campshare.web;
import com.example.campshare.dashboard.DashboardMapper; import org.springframework.stereotype.Controller; import org.springframework.ui.Model; import org.springframework.web.bind.annotation.GetMapping;
@Controller public class AdminDashboardController { private final DashboardMapper mapper; public AdminDashboardController(DashboardMapper mapper){this.mapper=mapper;} @GetMapping("/admin/dashboard") public String dashboard(Model model){model.addAttribute("summary",mapper.findSummary());return "admin/dashboard";} }
