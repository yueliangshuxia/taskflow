package com.taskflow.controller.admin;

import com.taskflow.entity.Task;
import com.taskflow.entity.enums.TaskStatus;
import com.taskflow.dao.TaskRepository;
import com.taskflow.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/tasks")
@RequiredArgsConstructor
public class AdminTaskController {

    private final TaskRepository taskRepository;
    private final AuditLogService auditLogService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Task> taskPage;

        if (status != null && !status.isEmpty()) {
            TaskStatus taskStatus = TaskStatus.valueOf(status);
            taskPage = taskRepository.findAllByStatusOrderByCreatedAtDesc(taskStatus, pageable);
        } else {
            taskPage = taskRepository.findAllByOrderByCreatedAtDesc(pageable);
        }

        model.addAttribute("tasks", taskPage.getContent());
        model.addAttribute("currentPage", taskPage.getNumber());
        model.addAttribute("totalPages", taskPage.getTotalPages());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("title", "任务管理");
        model.addAttribute("content", "admin/tasks");
        return "layout/base";
    }

    @PostMapping("/{id}/delete")
    public String deleteTask(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            String taskTitle = taskRepository.findById(id)
                    .map(Task::getTitle).orElse("(unknown)");
            taskRepository.deleteById(id);
            auditLogService.log("DELETE", "Task", id,
                    "管理员删除任务: " + taskTitle, "admin");
            redirectAttrs.addFlashAttribute("message", "任务已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/tasks";
    }
}
