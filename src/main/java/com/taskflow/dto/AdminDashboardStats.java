package com.taskflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
public class AdminDashboardStats {
    private long totalUsers;
    private long totalProjects;
    private long totalTasks;
    private long todayVisits;
    private long totalVisits;
    private Map<String, Long> tasksByStatus;
    private Map<String, Long> tasksByPriority;
}
