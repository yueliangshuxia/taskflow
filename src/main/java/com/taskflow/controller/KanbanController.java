package com.taskflow.controller;

import com.taskflow.dto.ProjectDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class KanbanController {

    private final TaskService taskService;
    private final ProjectService projectService;

    @GetMapping("/projects/{projectId}/kanban")
    public String kanbanView(@PathVariable Long projectId, Model model, Authentication auth) {
        ProjectDto project = projectService.findById(projectId);
        List<TaskDto> allTasks = taskService.findAllByProjectId(projectId);

        // Group by status
        Map<String, List<TaskDto>> grouped = allTasks.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getStatus() != null ? t.getStatus().name() : "TODO",
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        // Ensure all statuses exist
        for (TaskStatus status : TaskStatus.values()) {
            grouped.putIfAbsent(status.name(), List.of());
        }

        // Sort by priority within each group
        grouped.values().forEach(list -> list.sort(Comparator.comparingInt(t -> {
            String p = t.getPriority() != null ? t.getPriority().name() : "LOW";
            return switch (p) {
                case "URGENT" -> 0;
                case "HIGH" -> 1;
                case "MEDIUM" -> 2;
                default -> 3;
            };
        })));

        model.addAttribute("project", project);
        model.addAttribute("todoTasks", grouped.getOrDefault("TODO", List.of()));
        model.addAttribute("inProgressTasks", grouped.getOrDefault("IN_PROGRESS", List.of()));
        model.addAttribute("inReviewTasks", grouped.getOrDefault("IN_REVIEW", List.of()));
        model.addAttribute("doneTasks", grouped.getOrDefault("DONE", List.of()));
        model.addAttribute("title", "看板 - " + project.getName());
        model.addAttribute("content", "users/kanban");
        return "layout/base";
    }
}
