package com.taskflow.controller;

import com.taskflow.service.TaskService;
import com.taskflow.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class GanttController {

    private final TaskService taskService;
    private final ProjectService projectService;

    @GetMapping("/projects/{projectId}/gantt")
    public String ganttView(@PathVariable Long projectId, Model model, Authentication auth) {
        var project = projectService.findById(projectId);
        var tasks = taskService.findAllByProjectId(projectId);

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks);
        model.addAttribute("title", "甘特图 - " + project.getName());
        model.addAttribute("content", "users/gantt");
        return "layout/base";
    }
}
