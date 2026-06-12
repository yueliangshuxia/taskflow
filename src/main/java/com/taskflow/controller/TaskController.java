package com.taskflow.controller;

import com.taskflow.dto.TaskCreateDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.dto.CommentDto;
import com.taskflow.service.CommentService;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;
import com.taskflow.service.FileStorageService;
import com.taskflow.entity.User;
import com.taskflow.service.UserService;
import com.taskflow.dao.TaskAttachmentRepository;
import com.taskflow.entity.TaskAttachment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final CommentService commentService;
    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final TaskAttachmentRepository attachmentRepository;

    @GetMapping("/projects/{projectId}/tasks/create")
    public String showCreateForm(@PathVariable Long projectId, Model model) {
        var project = projectService.findById(projectId);
        var members = projectService.getProjectMembers(projectId);
        model.addAttribute("project", project);
        model.addAttribute("members", members);
        model.addAttribute("task", new TaskCreateDto());
        model.addAttribute("title", "创建任务 - " + project.getName());
        model.addAttribute("content", "users/task-form");
        return "layout/base";
    }

    @PostMapping("/projects/{projectId}/tasks/create")
    public String createTask(@PathVariable Long projectId,
                             @ModelAttribute TaskCreateDto createDto,
                             Authentication auth,
                             RedirectAttributes redirectAttrs) {
        try {
            taskService.createTask(projectId, createDto, auth.getName());
            redirectAttrs.addFlashAttribute("message", "任务创建成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/tasks/{id}")
    public String taskDetail(@PathVariable Long id, Model model, Authentication auth) {
        TaskDto task = taskService.findById(id);
        List<CommentDto> comments = commentService.getTaskComments(id);
        var members = projectService.getProjectMembers(task.getProjectId());

        List<TaskAttachment> attachments = attachmentRepository.findByTaskId(id);

        model.addAttribute("task", task);
        model.addAttribute("comments", comments);
        model.addAttribute("attachments", attachments);
        model.addAttribute("members", members != null ? members : List.of());
        model.addAttribute("title", "任务 - " + task.getTitle());
        model.addAttribute("content", "users/task-detail");
        return "layout/base";
    }

    @GetMapping("/tasks/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        TaskDto task = taskService.findById(id);
        var members = projectService.getProjectMembers(task.getProjectId());
        model.addAttribute("task", task);
        model.addAttribute("members", members != null ? members : List.of());
        model.addAttribute("title", "编辑任务");
        model.addAttribute("content", "users/task-form");
        return "layout/base";
    }

    @PostMapping("/tasks/{id}/edit")
    public String updateTask(@PathVariable Long id, @ModelAttribute TaskCreateDto updateDto,
                             Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            taskService.updateTask(id, updateDto, auth.getName());
            redirectAttrs.addFlashAttribute("message", "任务更新成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/delete")
    public String deleteTask(@PathVariable Long id, Authentication auth,
                             RedirectAttributes redirectAttrs) {
        TaskDto task = taskService.findById(id);
        Long projectId = task.getProjectId();
        try {
            taskService.deleteTask(id, auth.getName());
            redirectAttrs.addFlashAttribute("message", "任务已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + (projectId != null ? projectId : 0);
    }

    @PostMapping("/tasks/{id}/comments")
    public String addComment(@PathVariable Long id, @RequestParam String content,
                             Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            commentService.addComment(id, content, auth.getName());
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tasks/" + id;
    }

    @PostMapping("/tasks/{id}/attachments")
    public String uploadAttachment(@PathVariable Long id,
                                   @RequestParam("file") MultipartFile file,
                                   Authentication auth,
                                   RedirectAttributes redirectAttrs) {
        try {
            User user = userService.getCurrentUser(auth.getName());
            fileStorageService.storeFile(file, id, user.getId());
            redirectAttrs.addFlashAttribute("message", "文件上传成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "文件上传失败: " + e.getMessage());
        }
        return "redirect:/tasks/" + id;
    }
}
