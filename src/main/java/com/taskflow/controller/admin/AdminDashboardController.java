package com.taskflow.controller.admin;

import com.taskflow.dto.AdminDashboardStats;
import com.taskflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        AdminDashboardStats stats = dashboardService.getAdminDashboardStats();
        model.addAttribute("stats", stats);
        model.addAttribute("title", "管理仪表盘");
        model.addAttribute("content", "admin/dashboard");
        return "layout/base";
    }
}
