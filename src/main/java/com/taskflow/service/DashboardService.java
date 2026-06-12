package com.taskflow.service;

import com.taskflow.dto.DashboardStats;
import com.taskflow.dto.AdminDashboardStats;

public interface DashboardService {
    DashboardStats getUserDashboardStats(String username);
    AdminDashboardStats getAdminDashboardStats();
}
