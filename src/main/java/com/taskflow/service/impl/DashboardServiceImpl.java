package com.taskflow.service.impl;

import com.taskflow.dto.DashboardStats;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.dao.ProjectRepository;
import com.taskflow.dao.TaskRepository;
import com.taskflow.dao.CommentRepository;
import com.taskflow.entity.User;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final CommentRepository commentRepository;

    @Override
    public DashboardStats getUserDashboardStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        long totalProjects = projectRepository.countAccessibleProjects(user.getId());
        long totalTasks = taskRepository.countAccessibleTasks(user.getId());
        long todoTasks = taskRepository.countAccessibleTasksByStatus(user.getId(), TaskStatus.TODO);
        long inProgressTasks = taskRepository.countAccessibleTasksByStatus(user.getId(), TaskStatus.IN_PROGRESS);
        long doneTasks = taskRepository.countAccessibleTasksByStatus(user.getId(), TaskStatus.DONE);

        long overdueTasks = taskRepository.findOverdueTasks().stream()
                .filter(t -> t.getAssignee() != null && t.getAssignee().getId().equals(user.getId()))
                .count();

        return DashboardStats.builder()
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .todoTasks(todoTasks)
                .inProgressTasks(inProgressTasks)
                .doneTasks(doneTasks)
                .overdueTasks(overdueTasks)
                .totalComments(0)
                .build();
    }

    @Override
    public com.taskflow.dto.AdminDashboardStats getAdminDashboardStats() {
        long totalUsers = userRepository.count();
        long totalProjects = projectRepository.count();
        long totalTasks = taskRepository.count();

        var tasksByStatus = taskRepository.countTasksByStatus();
        java.util.Map<String, Long> statusMap = new java.util.HashMap<>();
        for (Object[] row : tasksByStatus) {
            statusMap.put(((TaskStatus) row[0]).getDisplayName(), (Long) row[1]);
        }

        var tasksByPriority = taskRepository.countTasksByPriority();
        java.util.Map<String, Long> priorityMap = new java.util.HashMap<>();
        for (Object[] row : tasksByPriority) {
            priorityMap.put(((com.taskflow.entity.enums.TaskPriority) row[0]).getDisplayName(), (Long) row[1]);
        }

        return com.taskflow.dto.AdminDashboardStats.builder()
                .totalUsers(totalUsers)
                .totalProjects(totalProjects)
                .totalTasks(totalTasks)
                .todayVisits(0)
                .totalVisits(0)
                .tasksByStatus(statusMap)
                .tasksByPriority(priorityMap)
                .build();
    }
}
