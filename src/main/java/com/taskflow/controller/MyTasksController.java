package com.taskflow.controller;

import com.taskflow.dto.TaskDto;
import com.taskflow.entity.User;
import com.taskflow.dao.UserRepository;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class MyTasksController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    @GetMapping("/my-tasks")
    public String myTasks(
            @RequestParam(defaultValue = "0") int todoPage,
            @RequestParam(defaultValue = "0") int progressPage,
            @RequestParam(defaultValue = "0") int reviewPage,
            @RequestParam(defaultValue = "0") int donePage,
            @RequestParam(defaultValue = "10") int size,
            Model model, Authentication auth) {

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        PageRequest pageable = PageRequest.of(todoPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskDto> todo = taskService.findFiltered(null,
                createFilter(TaskStatus.TODO, user.getId()), pageable);
        Page<TaskDto> inProgress = taskService.findFiltered(null,
                createFilter(TaskStatus.IN_PROGRESS, user.getId()),
                PageRequest.of(progressPage, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<TaskDto> inReview = taskService.findFiltered(null,
                createFilter(TaskStatus.IN_REVIEW, user.getId()),
                PageRequest.of(reviewPage, size, Sort.by(Sort.Direction.DESC, "createdAt")));
        Page<TaskDto> done = taskService.findFiltered(null,
                createFilter(TaskStatus.DONE, user.getId()),
                PageRequest.of(donePage, size, Sort.by(Sort.Direction.DESC, "createdAt")));

        model.addAttribute("todoPage", todo);
        model.addAttribute("inProgressPage", inProgress);
        model.addAttribute("inReviewPage", inReview);
        model.addAttribute("donePage", done);
        model.addAttribute("title", "我的任务");
        model.addAttribute("content", "users/my-tasks");
        return "layout/base";
    }

    private com.taskflow.dto.TaskFilter createFilter(TaskStatus status, Long userId) {
        com.taskflow.dto.TaskFilter filter = new com.taskflow.dto.TaskFilter();
        filter.setStatus(status.name());
        filter.setAssigneeId(userId);
        return filter;
    }
}
