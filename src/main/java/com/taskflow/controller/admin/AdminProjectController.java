package com.taskflow.controller.admin;

import com.taskflow.entity.Project;
import com.taskflow.dao.ProjectRepository;
import com.taskflow.service.AuditLogService;
import jakarta.persistence.EntityManager;
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
@RequestMapping("/admin/projects")
@RequiredArgsConstructor
public class AdminProjectController {

    private final ProjectRepository projectRepository;
    private final EntityManager entityManager;
    private final AuditLogService auditLogService;

    @GetMapping
    @Transactional(readOnly = true)
    public String listProjects(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Project> projectPage = projectRepository.findAllByOrderByCreatedAtDesc(pageable);

        model.addAttribute("projects", projectPage.getContent());
        model.addAttribute("currentPage", projectPage.getNumber());
        model.addAttribute("totalPages", projectPage.getTotalPages());
        model.addAttribute("title", "项目管理");
        model.addAttribute("content", "admin/projects");
        return "layout/base";
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@PathVariable Long id, RedirectAttributes redirectAttrs) {
        try {
            String projectName = projectRepository.findById(id)
                    .map(Project::getName).orElse("(unknown)");
            projectRepository.deleteById(id);
            auditLogService.log("DELETE", "Project", id,
                    "管理员删除项目: " + projectName, "admin");
            redirectAttrs.addFlashAttribute("message", "项目已删除");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "删除失败: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }

    @PostMapping("/reset-id")
    @Transactional
    public String resetProjectId(RedirectAttributes redirectAttrs) {
        if (projectRepository.count() > 0) {
            redirectAttrs.addFlashAttribute("error", "请先删除所有项目后再重置 ID");
            return "redirect:/admin/projects";
        }
        try {
            entityManager.createNativeQuery("ALTER TABLE projects AUTO_INCREMENT = 1").executeUpdate();
            auditLogService.log("RESET_ID", "Project", null,
                    "管理员重置项目自增 ID", "admin");
            redirectAttrs.addFlashAttribute("message", "项目 ID 已重置成功，新项目 ID 将从 1 开始");
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "重置失败: " + e.getMessage());
        }
        return "redirect:/admin/projects";
    }
}
