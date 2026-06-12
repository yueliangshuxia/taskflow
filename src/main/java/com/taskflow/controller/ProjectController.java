package com.taskflow.controller;

import com.taskflow.dto.ProjectDto;
import com.taskflow.dto.TaskDto;
import com.taskflow.entity.User;
import com.taskflow.dao.UserRepository;
import com.taskflow.service.ProjectService;
import com.taskflow.service.TaskService;
import com.taskflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final TaskService taskService;
    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping
    public String listProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model, Authentication auth) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ProjectDto> projectPage = projectService.findAccessibleProjects(auth.getName(), pageable);

        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("currentPage", projectPage.getNumber());
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("totalItems", projectPage.getTotalElements());
        model.addAttribute("title", "我的项目");
        model.addAttribute("content", "users/projects");
        return "layout/base";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("project", new ProjectDto());
        model.addAttribute("title", "创建项目");
        model.addAttribute("content", "users/project-form");
        return "layout/base";
    }

    @PostMapping("/create")
    public String createProject(@ModelAttribute ProjectDto projectDto,
                                Authentication auth,
                                RedirectAttributes redirectAttrs) {
        try {
            projectService.createProject(projectDto.getName(), projectDto.getDescription(), auth.getName());
            redirectAttrs.addFlashAttribute("message", "项目创建成功");
            return "redirect:/projects";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
            return "redirect:/projects/create";
        }
    }

    @GetMapping("/{id}")
    public String projectDetail(@PathVariable Long id,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "50") int size,
                                Model model, Authentication auth) {
        ProjectDto project = projectService.findById(id);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TaskDto> tasks = taskService.findByProjectId(id, pageable);
        var members = projectService.getProjectMembers(id);

        // 获取所有用户（用于添加成员下拉框）
        List<User> allUsers = userRepository.findAll();
        List<Long> memberIds = members.stream().map(User::getId).toList();
        List<User> availableUsers = allUsers.stream()
                .filter(u -> !memberIds.contains(u.getId()))
                .toList();

        model.addAttribute("project", project);
        model.addAttribute("tasks", tasks.getContent());
        model.addAttribute("members", members);
        model.addAttribute("availableUsers", availableUsers);
        model.addAttribute("currentUsername", auth.getName());
        model.addAttribute("title", "项目 - " + project.getName());
        model.addAttribute("content", "users/project-detail");
        return "layout/base";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        ProjectDto project = projectService.findById(id);
        model.addAttribute("project", project);
        model.addAttribute("title", "编辑项目");
        model.addAttribute("content", "users/project-form");
        return "layout/base";
    }

    @PostMapping("/{id}/edit")
    public String updateProject(@PathVariable Long id, @ModelAttribute ProjectDto projectDto,
                                Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            projectService.updateProject(id, projectDto.getName(), projectDto.getDescription(), auth.getName());
            redirectAttrs.addFlashAttribute("message", "项目更新成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, Authentication auth,
                                RedirectAttributes redirectAttrs) {
        try {
            projectService.deleteProject(id, auth.getName());
            redirectAttrs.addFlashAttribute("message", "项目已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects";
    }

    @PostMapping("/{id}/members/add")
    public String addMember(@PathVariable Long id, @RequestParam Long userId,
                            Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            projectService.addMember(id, userId, auth.getName());
            redirectAttrs.addFlashAttribute("message", "成员添加成功");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/members/remove")
    public String removeMember(@PathVariable Long id, @RequestParam Long userId,
                               Authentication auth, RedirectAttributes redirectAttrs) {
        try {
            projectService.removeMember(id, userId, auth.getName());
            redirectAttrs.addFlashAttribute("message", "成员已移除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + id;
    }
}
