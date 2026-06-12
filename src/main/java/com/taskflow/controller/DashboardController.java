package com.taskflow.controller;

import com.taskflow.dto.DashboardStats;
import com.taskflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        DashboardStats stats = dashboardService.getUserDashboardStats(auth.getName());
        model.addAttribute("stats", stats);
        model.addAttribute("title", "仪表盘");
        model.addAttribute("content", "users/dashboard");
        return "layout/base";
    }
}
